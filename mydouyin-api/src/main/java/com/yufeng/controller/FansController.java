package com.yufeng.controller;

import com.yufeng.base.BaseInfoProperties;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.pojo.Users;
import com.yufeng.service.FanService;
import com.yufeng.service.UserService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Lzm
 * @CreateTime 2025年5月05日 18:05
 */

@Slf4j
@RestController
@Api(tags = "FansController 粉丝相关业务功能的接口") // 这个注解可以用来定义接口类的名字
@RequestMapping("fans") // 这个注解可以用来定义接口类的请求路径
public class FansController extends BaseInfoProperties {

    @Autowired
    private FanService fanService;

    @Autowired
    private UserService userService;


    /**
     * 关注当前视频的博主
     */
    @PostMapping("follow")
    public GraceJSONResult doFollow(@RequestParam String myId,
                         @RequestParam String vlogerId) {

        log.info("关注的参数是：myId = {}, vlogerId = {}", myId, vlogerId);

        // 判断两个id不能为空
        if(StringUtils.isBlank(myId) || StringUtils.isBlank(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }
        // 判断当前用户，不能关注自己
        if(myId.equalsIgnoreCase(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        // 判断两个id的用户是否存在
        Users myInfo = userService.getUser(myId);
        Users vloger = userService.getUser(vlogerId);
        if(myInfo == null || vloger == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }


        // 关注当前视频的博主
        fanService.doFollow(myId, vlogerId);


        // 这里用户主页中的粉丝数和关注数，属于前端经常要拿的数据，那我放到redis中
        // 我的关注数 +1，博主的粉丝数 +1
        redis.increment(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
        redis.increment(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);
        // 我和博主的关联关系(也就是我关注了博主这个关系)依赖Redis，不要存储到数据库，避免db的性能瓶颈「如果不这样，查询我的粉丝列表时候，就得一个一个记录去查询fans表我是否有关注」
        redis.set(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId, "1");

        return GraceJSONResult.ok();
    }

    /**
     * 取消关注
     */
    @PostMapping("cancel")
    public GraceJSONResult doCancel(@RequestParam String myId,
                                    @RequestParam String vlogerId) {
        // 马上执行service层的取消关注
        fanService.doCancel(myId, vlogerId);

        // 然后将博主的粉丝数 -1，我的关注数 -1
        redis.decrement(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
        redis.decrement(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);
        // 我和博主的关联关系，依赖Redis，不要存储到数据库，避免db的性能瓶颈
        redis.del(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId);

        return GraceJSONResult.ok();
    }


    /**
     * 查询当前用户是否关注了该视频博主(底层是通过查询fans表来判断的，如果查不到记录，说明没有关注)
     */
    @GetMapping("queryDoIFollowVloger")
    public GraceJSONResult queryDoIFollowVloger(@RequestParam String myId,
                                                @RequestParam String vlogerId) {
        // 将两个参数传给service层
        boolean bool = fanService.queryDoIFollowVloger(myId, vlogerId);
        return GraceJSONResult.ok(bool);
    }

    /**
     * 查询当前用户关注的视频博主列表
     */
    @GetMapping("queryMyFollows")
    public GraceJSONResult queryMyFollows(@RequestParam String myId,
                                          @RequestParam Integer page,
                                          @RequestParam Integer pageSize) {

        log.info("查询当前用户关注的视频博主列表，参数为：myId = {}, page = {}, pageSize = {}", myId, page, pageSize);

        return GraceJSONResult.ok(fanService.queryMyFollows(myId, page, pageSize));
    }



    /**
     * 查询当前用户粉丝列表
     */
    @GetMapping("queryMyFans")
    public GraceJSONResult queryMyFans(@RequestParam String myId,
                                          @RequestParam Integer page,
                                          @RequestParam Integer pageSize) {

        log.info("查询当前用户关注的粉丝列表，参数为：myId = {}, page = {}, pageSize = {}", myId, page, pageSize);

        return GraceJSONResult.ok(fanService.queryMyFans(myId, page, pageSize));
    }

}
