package com.yufeng.base;

import com.github.pagehelper.PageInfo;
import com.yufeng.utils.PagedGridResult;
import com.yufeng.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Lzm
 * 这个类是用来存放一些公共的常量和方法的，比如Redis的bean对象、Redis的key值、常量等
 * 注意: 这个类被PassportController类所继承，所以不用担心Redis的bean对象不能使用
 */
public class BaseInfoProperties {

    @Autowired
    public RedisOperator redis;

    public static final Integer COMMON_START_PAGE = 1; // 默认从第1页开始
    public static final Integer COMMON_START_PAGE_ZERO = 0;
    public static final Integer COMMON_PAGE_SIZE = 10; // 默认1页有10条数据

    public static final String MOBILE_SMSCODE = "mobile:smscode";
    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_USER_INFO = "redis_user_info";

    // 短视频的评论总数
    public static final String REDIS_VLOG_COMMENT_COUNTS = "redis_vlog_comment_counts";
    // 短视频的评论喜欢数量
    public static final String REDIS_VLOG_COMMENT_LIKED_COUNTS = "redis_vlog_comment_liked_counts";
    // 用户点赞评论
    public static final String REDIS_USER_LIKE_COMMENT = "redis_user_like_comment";

    // 我的关注总数
    public static final String REDIS_MY_FOLLOWS_COUNTS = "redis_my_follows_counts";
    // 我的粉丝总数
    public static final String REDIS_MY_FANS_COUNTS = "redis_my_fans_counts";
    // 博主和粉丝的关联关系，用于判断他们是否互粉
    public static final String REDIS_FANS_AND_VLOGGER_RELATIONSHIP = "redis_fans_and_vlogger_relationship";

    // 视频和发布者获赞数
    public static final String REDIS_VLOG_BE_LIKED_COUNTS = "redis_vlog_be_liked_counts";
    public static final String REDIS_VLOGER_BE_LIKED_COUNTS = "redis_vloger_be_liked_counts";

    // 用户是否喜欢/点赞视频，取代数据库的关联关系，1：喜欢，0：不喜欢（默认） redis_user_like_vlog:{userId}:{vlogId}
    public static final String REDIS_USER_LIKE_VLOG = "redis_user_like_vlog";


//    public Map<String, String> getErrors(BindingResult result) {
//        Map<String, String> map = new HashMap<>();
//        List<FieldError> errorList = result.getFieldErrors();
//        for (FieldError ff : errorList) {
//            // 错误所对应的属性字段名
//            String field = ff.getField();
//            // 错误的信息
//            String msg = ff.getDefaultMessage();
//            map.put(field, msg);
//        }
//        return map;
//    }


    /**
     * 这个方法的作用是将分页查询的结果封装到PagedGridResult对象中，因为前端需要相关的分页信息，不单单是数据列表
     */
    public PagedGridResult setterPagedGrid(List<?> list, Integer page) {
        // 分页查询
        PageInfo<?> pageList = new PageInfo<>(list); // 这个PageInfo对象能够获取到分页的相关信息，比如总页数、总记录数等
        // 封装到PagedGridResult对象中
        PagedGridResult gridResult = new PagedGridResult();
        // 设置分页信息
        gridResult.setRows(list);
        gridResult.setPage(page);
        gridResult.setRecords(pageList.getTotal());
        gridResult.setTotal(pageList.getPages());
        return gridResult;
    }
}
