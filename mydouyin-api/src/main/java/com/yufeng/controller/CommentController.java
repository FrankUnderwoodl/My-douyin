package com.yufeng.controller;

import com.yufeng.base.BaseInfoProperties;
import com.yufeng.base.RabbitMQConfig;
import com.yufeng.bo.CommentBO;
import com.yufeng.enums.MessageEnum;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.mo.MessageMO;
import com.yufeng.pojo.Comment;
import com.yufeng.pojo.Vlog;
import com.yufeng.service.CommentService;
import com.yufeng.service.MsgService;
import com.yufeng.service.VlogService;
import com.yufeng.service.impl.MsgServiceImpl;
import com.yufeng.utils.JsonUtils;
import com.yufeng.utils.PagedGridResult;
import com.yufeng.vo.CommentVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lzm
 * @CreateTime 2025年5月05日 18:05
 */

@Slf4j
@RestController
@Api(tags = "CommentController 评论模块相关业务功能的接口") // 这个注解可以用来定义接口类的名字
@RequestMapping("comment") // 这个注解可以用来定义接口类的请求路径
public class CommentController extends BaseInfoProperties {

    @Autowired
    private CommentService commentService;

    @Autowired
    private VlogService vlogService; // 在这里，我们需要获取视频的封面和id等信息，所以需要注入VlogService

    @Autowired
    private MsgService msgService;
    @Autowired
    private RabbitTemplate rabbitTemplate;


    /**
     * 用户发表一条评论(也就是把你写的评论记录存到数据库中，还有当前评论数在Redis中累加)
     */
    @PostMapping("create")
    public GraceJSONResult create(@RequestBody @Valid CommentBO commentBO) {
        // 1. 发表评论
        CommentVO commentVO = commentService.createComment(commentBO);
        // 2. 返回结果
        return GraceJSONResult.ok(commentVO);
    }


    /**
     * 查询当前视频的评论总数(直接通过查询Redis获取)
     */
    @GetMapping("counts")
    public GraceJSONResult counts(@RequestParam String vlogId) {
        // 查询Redis
        String commentCount = redis.get(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId);
        if (StringUtils.isBlank(commentCount) || commentCount.equalsIgnoreCase("0")) {
            return GraceJSONResult.ok(0);
        } else {
            return GraceJSONResult.ok(Integer.valueOf(commentCount));
        }
    }


    /**
     * 查询当前视频的评论列表
     */
    @GetMapping("list")
    public GraceJSONResult list(
            @RequestParam String vlogId,
            @RequestParam(defaultValue = "") String userId,
            @RequestParam Integer page,
            @RequestParam Integer pageSize) {
        // 1.参数校验
        log.info("查询视频评论列表的参数是：vlogId={}, userId={}, page={}, pageSize={}", vlogId, userId, page, pageSize);

        // 2.直接调用service层的方法
        List<CommentVO> commentList = commentService.getCommentList(vlogId, userId, page, pageSize);
        // 3.返回结果
        return GraceJSONResult.ok(commentList);
    }


    /**
     * 删除评论
     */
    @DeleteMapping("delete")
    public GraceJSONResult delete(
            @RequestParam String commentUserId,
            @RequestParam String commentId,
            @RequestParam String vlogId
            ) {
        // 1. 删除评论
        commentService.deleteComment(commentUserId, commentId, vlogId);
        // 2. 返回结果
        return GraceJSONResult.ok();
    }


    /**
     * 点赞评论接口
     */
    @PostMapping("like")
    public GraceJSONResult like(
            @RequestParam String commentId,
            @RequestParam String userId) {

        // 直接把记录(什么记录？①该视频的评论有多少 ②当前用户是否喜欢该评论)存入Redis中，不用存入MySQL。
        // incrementHash的底层理解：这里的key是REDIS_VLOG_COMMENT_LIKED_COUNTS，value是一个map，value里面的key是评论的id，value是点赞的数量
        // 这里其实有问题，big-key问题,如果评论的数量过多，Redis就会很卡。
        // 解决方式：把commentId再进行hash分片，要不然的话，你索性使用单key来increment
        redis.incrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId, 1); // 某个评论的点赞数+1
        redis.setHashValue(REDIS_USER_LIKE_COMMENT, userId+":"+commentId,"1"); // 设置当前userId已经点赞了该评论(存入Redis中)。


        // 系统消息
        // 这里可以发送系统消息，告诉评论人有其他人点赞了他的评论
        Map<String, String> msgContent = new HashMap<>();
        // 获取视频的封面、id
        Comment comment = commentService.getComment(commentId); // 先通过‘评论主键’获取‘评论pojo’
        Vlog vlog = vlogService.getVlog(comment.getVlogId()); // 再通过‘评论pojo’获取‘视频pojo’
        msgContent.put("vlogId", vlog.getId()); // 视频的id
        msgContent.put("vlogCover", vlog.getCover()); // 视频的封面
        msgContent.put("commentId", commentId); // 评论信息的id


        // msgService.createMsg(userId, comment.getCommentUserId(), MessageEnum.LIKE_COMMENT.type, msgContent);

        // 创建一个MO给RabbitMQ作为消息发送
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(userId); // 设置发送者ID
        messageMO.setToUserId(comment.getCommentUserId()); // 设置接收者ID
        messageMO.setMsgContent(msgContent); // 设置消息内容

        // 交给RabbitMQ生产消息(异步执行，不会阻塞当前线程)
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG, // 要发送交换机的名称
                "sys.msg" + MessageEnum.LIKE_COMMENT.enValue, // 路由键(规定消息的路由规则，也就是交换机将消息发送到哪个队列)
                JsonUtils.objectToJson(messageMO)); // 消息具体内容

        return GraceJSONResult.ok();
    }


    /**
     * 取消点赞评论
     */
    @DeleteMapping("unlike")
    public GraceJSONResult unlike(
            @RequestParam String commentId,
            @RequestParam String userId) {

        redis.decrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId, 1);
        redis.hdel(REDIS_USER_LIKE_COMMENT, userId+":"+commentId);
        return GraceJSONResult.ok();
    }
}
