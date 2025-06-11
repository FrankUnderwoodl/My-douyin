package com.yufeng.controller;

import com.yufeng.base.BaseInfoProperties;
import com.yufeng.bo.VlogBO;
import com.yufeng.enums.YesOrNo;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.service.VlogService;
import com.yufeng.utils.PagedGridResult;
import com.yufeng.vo.IndexVlogVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

/**
 * @author Lzm
 * @CreateTime 2025年5月05日 18:05
 * @Describe 每当用户发布一个新的短视频发布时，都会调用这个接口
 */
@Slf4j
@RestController
@Api(tags = "VlogController 短视频相关业务功能的接口") // 这个注解可以用来定义接口类的名字
@RequestMapping("vlog")
@RefreshScope // 这个注解是SpringCloud的原生注解，用来实现配置中心的动态刷新(其实就是在配置中心修改了配置后，自动刷新到当前应用中。这里的配置中心具体指的是Nacos)
public class VlogController extends BaseInfoProperties {

    // service层
    @Autowired
    private VlogService vlogService;

    @Value("${nacos.counts}")
    private Integer nacosCounts; // 这个值会从Nacos配置中心获取

    /**
     * 上传短视频接口
     */
    @PostMapping("publish")
    public GraceJSONResult publish(@RequestBody VlogBO vlog) {
        // 打印日志
        log.info("发布短视频的参数是：{}", vlog);

        vlogService.createVlog(vlog);
        return GraceJSONResult.ok(vlog);
    }



    /**
     * 查询短视频接口
     * @Describe 这个接口的作用是:将用户在首页刷到的短视频列表返回给前端
     * ‘@RequestParam(defaultValue = "")’，defaultValue的意思是如果没有传入参数，则默认值为空字符串(这样在knife4J就不需要传入参数了)
     * @param page 从第几页开始
     * @param pageSize 每页多少条数据
     */
    @GetMapping("indexList")
    public GraceJSONResult indexList(@RequestParam(defaultValue = "") String userId,
                                     @RequestParam(defaultValue = "") String search,
                                     @RequestParam Integer page,
                                     @RequestParam Integer pageSize) {
        // 打印日志
        log.info("查询短视频列表的参数是：search={}, page={}, pageSize={}", search, page, pageSize);

        // 判空处理
        if(page == null) {
            page = COMMON_START_PAGE; // 默认从第一页开始
        }
        if(pageSize == null) {
            pageSize = COMMON_PAGE_SIZE; // 默认每页有10条数据
        }

        // 调用service层的方法
        PagedGridResult pagedGridResult = vlogService.getIndexVlogList(userId,search, page, pageSize);
        return GraceJSONResult.ok(pagedGridResult);
    }



    /**
     * 查询当前短视频的详情(不含tab页)
     */
    @GetMapping("detail")
    public GraceJSONResult detail(@RequestParam(defaultValue = "") String userId,
                                  @RequestParam String vlogId) {
        // 打印日志
        log.info("查询短视频详情的参数(vlogID)是：{}", vlogId);

        IndexVlogVO indexVlogVO = vlogService.getVlogDetailById(userId ,vlogId);
        return GraceJSONResult.ok(indexVlogVO);
    }



    /**
     * 将视频改为私密
     */
    @PostMapping("changeToPrivate")
    public GraceJSONResult changeToPrivate(@RequestParam String userId,
                                          @RequestParam String vlogerId) {
        // 打印日志
        log.info("将视频改为私密的参数是：userId={}, vlogerId={}", userId, vlogerId);

        vlogService.changeToPrivateOrPublish(userId, vlogerId, YesOrNo.YES.type);
        return GraceJSONResult.ok();
    }



    /**
     * 将视频改为公开
     */
    @PostMapping("changeToPublic")
    public GraceJSONResult changeToPublic(@RequestParam String userId,
                                          @RequestParam String vlogerId) {
        // 打印日志
        log.info("将视频改为公开的参数是：userId={}, vlogerId={}", userId, vlogerId);

        vlogService.changeToPrivateOrPublish(userId, vlogerId, YesOrNo.NO.type);
        return GraceJSONResult.ok();
    }



    /**
     * 查询我的公开短视频列表接口
     */
    @GetMapping("myPublicList")
    public GraceJSONResult myPublicList(@RequestParam String userId,
                                     @RequestParam Integer page,
                                     @RequestParam Integer pageSize) {
        // 打印日志
        log.info("查询短视频列表(公开)的参数是：userId={}, page={}, pageSize={}", userId, page, pageSize);

        // 判空处理
        if(page == null) {
            page = COMMON_START_PAGE; // 默认从第一页开始
        }
        if(pageSize == null) {
            pageSize = COMMON_PAGE_SIZE; // 默认每页有10条数据
        }

        // 调用service层的方法
        PagedGridResult pagedGridResult = vlogService.queryMyVlogList(userId, page, pageSize, YesOrNo.NO.type);
        return GraceJSONResult.ok(pagedGridResult);
    }



    /**
     * 查询我的私密视频列表接口
     */
    @GetMapping("myPrivateList")
    public GraceJSONResult myPrivateList(@RequestParam String userId,
                                     @RequestParam Integer page,
                                     @RequestParam Integer pageSize) {
        // 打印日志
        log.info("查询短视频列表(私密)的参数是：userId={}, page={}, pageSize={}", userId, page, pageSize);

        // 判空处理
        if(page == null) {
            page = COMMON_START_PAGE; // 默认从第一页开始
        }
        if(pageSize == null) {
            pageSize = COMMON_PAGE_SIZE; // 默认每页有10条数据
        }

        // 调用service层的方法
        PagedGridResult pagedGridResult = vlogService.queryMyVlogList(userId, page, pageSize, YesOrNo.YES.type);
        return GraceJSONResult.ok(pagedGridResult);
    }



    /**
     * 查询我喜欢的视频列表接口
     */
    @GetMapping("myLikedList")
    public GraceJSONResult myLikedList(@RequestParam String userId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {
        // 打印日志
        log.info("查询我喜欢的视频列表参数是：userId={}, page={}, pageSize={}", userId, page, pageSize);

        // 判空处理
        if(page == null) {
            page = COMMON_START_PAGE; // 默认从第一页开始
        }
        if(pageSize == null) {
            pageSize = COMMON_PAGE_SIZE; // 默认每页有10条数据
        }

        // 调用service层的方法
        PagedGridResult pagedGridResult = vlogService.getMyLikedVlogList(userId, page, pageSize);
        return GraceJSONResult.ok(pagedGridResult);
    }


    /**
     * 用户点赞视频接口
     * @Describe 这个接口的作用是:用户对某个视频进行点赞操作
     * @param userId 用户id
     * @param vlogerId 视频发布者id
     * @param vlogId 视频id
     */
    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String userId,
                                @RequestParam String vlogerId,
                                @RequestParam String vlogId) {
        // 额外作业: userId、vlogerId、vlogId都不能为空，都需要存在

        // 打印日志
        log.info("用户点赞的参数是：userId={}, vlogerId={}, vlogId={}", userId, vlogerId, vlogId);

        // 调用service层的方法
        vlogService.userLikeVlog(userId, vlogId);


        // 把用户点赞视频的记录，存入Redis中
        // ①用户点赞后，视频被点赞数+1、视频发布者的获赞数+1(这个总数量是前端需要去显示的，具体在@indexList中会用到)
        redis.increment(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);
        redis.increment(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);
        // ②我点赞的视频，需要在redis中保存关联关系(这个则是前端用来判断当前视频是否需要显示点赞的)
        redis.set(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId, "1");


        /**
         * 点赞完毕，获得当前在Redis中视频的点赞总数
         * 比如获得总计数为 1k/1w/10w，假定阈值为2000
         * 此时需要判断当前视频的点赞总数是否超过2000，如果超过，则需要将视频的点赞数存入Mysql中
         */
        // 获取当前视频的点赞总数
        String likedCounts = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
        log.info(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId + "的点赞总数为: {}", likedCounts);

        // 如果当前视频的点赞总数超过了阈值，则需要将视频的点赞数存入Mysql中
        // FIXME: 如果超过了阈值，每次点赞都会将Redis数据持久化到Mysql中，这样会导致Mysql的压力过大。
        if (StringUtils.isNotBlank(likedCounts) && Integer.parseInt(likedCounts) >= this.nacosCounts) {
            // 调用service层的方法，将点赞数存入Mysql中
            vlogService.flushCountsToMySQL(vlogId, Integer.parseInt(likedCounts));
        }
        return GraceJSONResult.ok();
    }



    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam String userId,
                                @RequestParam String vlogerId,
                                @RequestParam String vlogId) {
        // 额外作业: userId、vlogerId、vlogId都不能为空，都需要存在

        // 打印日志
        log.info("用户取消点赞的参数是：userId={}, vlogerId={}, vlogId={}", userId, vlogerId, vlogId);

        // 调用service层的方法
        vlogService.userUnLikeVlog(userId, vlogId);


        // Redis层
        // ①取消点赞后，视频、视频发布者的获赞数都累减1(这个总数量是前端需要显示的)
        redis.decrement(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);
        redis.decrement(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);
        // ②我点赞的视频，需要在redis中‘删除’关联关系
        redis.del(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId);

        return GraceJSONResult.ok();
    }


    /**
     * 查询视频的点赞总数，用于刷新视频的时候(因为其他用户也可能点赞了，底层查询的时候，用的是Redis)
     */
    @PostMapping("totalLikedCounts")
    public GraceJSONResult totalLikedCounts(@RequestParam String vlogId) {
        // 打印日志
        log.info("查询视频的点赞总数的参数是：vlogId={}", vlogId);

        // 调用service层的方法
        Integer likedCounts = vlogService.getVlogBeLikedCounts(vlogId);
        return GraceJSONResult.ok(likedCounts);
    }



    /**
     * 查询我关注博主发布的视频列表
     */
    @GetMapping("followList")
    public GraceJSONResult followList(@RequestParam String myId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {
        // 打印日志
        log.info("查询我关注的博主所发布视频的参数是：userId={}, page={}, pageSize={}", myId, page, pageSize);

        // 判空处理
        if(page == null) {
            page = COMMON_START_PAGE; // 默认从第一页开始
        }
        if(pageSize == null) {
            pageSize = COMMON_PAGE_SIZE; // 默认每页有10条数据
        }

        // 调用service层的方法
        PagedGridResult pagedGridResult = vlogService.getMyFollowVlogList(myId, page, pageSize);
        return GraceJSONResult.ok(pagedGridResult);
    }



    /**
     * 查询我朋友所发布的视频列表
     */
    @GetMapping("friendList")
    public GraceJSONResult friendList(@RequestParam String myId,
                                      @RequestParam Integer page,
                                      @RequestParam Integer pageSize) {
        // 打印日志
        log.info("查询我朋友所发布视频的参数是：userId={}, page={}, pageSize={}", myId, page, pageSize);

        // 判空处理
        if(page == null) {
            page = COMMON_START_PAGE; // 默认从第一页开始
        }
        if(pageSize == null) {
            pageSize = COMMON_PAGE_SIZE; // 默认每页有10条数据
        }

        // 调用service层的方法
        PagedGridResult pagedGridResult = vlogService.getMyFriendVlogList(myId, page, pageSize);
        return GraceJSONResult.ok(pagedGridResult);
    }
}
