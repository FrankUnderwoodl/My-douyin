package com.yufeng.controller;

import com.yufeng.bo.UpdatedUserBO;
import com.yufeng.enums.UserInfoModifyType;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.pojo.Users;
import com.yufeng.service.UserService;
import com.yufeng.vo.UsersVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Lzm
 * @CreateTime 2025年5月05日 18:05
 */

@Slf4j
@RestController
@Api(tags = "UserInfoController 用户信息接口模块") // 这个注解可以用来定义接口类的名字
@RequestMapping("/userInfo")
public class UserInfoController extends BaseInfoProperties{

    @Autowired
    private UserService userService;

    @GetMapping("/query")
    public Object query(@RequestParam String userId) {

        Users user = this.userService.getUser(userId);
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);

        // 我的关注博主总数量
        String myFollowsCountsStr = redis.get(REDIS_MY_FOLLOWS_COUNTS + ":" + userId);
        // 我的粉丝总数
        String myFansCountsStr = redis.get(REDIS_MY_FANS_COUNTS + ":" + userId);
        // 用户获赞总数，视频博主（点赞/喜欢）总和
        String likedVlogCountsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + userId);
        String likedVlogerCountsStr = redis.get(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + userId);

        // 设置数据
        Integer myFollowsCounts = 0;
        Integer myFansCounts = 0;
        Integer likedVlogCounts = 0;
        Integer likedVlogerCounts = 0;
        Integer totalLikeMeCounts = 0;

        if (StringUtils.isNotBlank(myFollowsCountsStr)) {
            myFollowsCounts = Integer.valueOf(myFollowsCountsStr);
        }
        if (StringUtils.isNotBlank(myFansCountsStr)) {
            myFansCounts = Integer.valueOf(myFansCountsStr);
        }
//        if (StringUtils.isNotBlank(likedVlogCountsStr)) {
//            likedVlogCounts = Integer.valueOf(likedVlogCountsStr);
//        }
        if (StringUtils.isNotBlank(likedVlogerCountsStr)) {
            likedVlogerCounts = Integer.valueOf(likedVlogerCountsStr);
        }
        totalLikeMeCounts = likedVlogCounts + likedVlogerCounts;

        usersVO.setMyFollowsCounts(myFollowsCounts);
        usersVO.setMyFansCounts(myFansCounts);
        usersVO.setTotalLikeMeCounts(totalLikeMeCounts);

        return GraceJSONResult.ok(usersVO);
    }


    // 这个接口对应前端类似这样的接口，url: serverUrl + "/userInfo/modifyUserInfo?type=1",
    @PostMapping("modifyUserInfo")
    public GraceJSONResult modifyUserInfo(@RequestBody UpdatedUserBO updatedUserBO, @RequestParam Integer type) {

        // 先判断前端传来的type类型是否正确，如果不正确就抛出异常，后面就不执行了
        UserInfoModifyType.checkUserInfoTypeIsRight(type);

        // 直接调用service层
        userService.UpdateUserInfo(updatedUserBO);



        return GraceJSONResult.ok();
    }
}
