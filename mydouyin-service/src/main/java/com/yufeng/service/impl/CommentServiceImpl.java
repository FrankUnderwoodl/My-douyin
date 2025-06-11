package com.yufeng.service.impl;

import com.github.pagehelper.PageHelper;
import com.yufeng.base.BaseInfoProperties;
import com.yufeng.base.RabbitMQConfig;
import com.yufeng.bo.CommentBO;
import com.yufeng.enums.MessageEnum;
import com.yufeng.mapper.CommentMapper;
import com.yufeng.mapper.CommentMapperCustom;
import com.yufeng.mo.MessageMO;
import com.yufeng.pojo.Comment;
import com.yufeng.pojo.Vlog;
import com.yufeng.service.CommentService;
import com.yufeng.service.MsgService;
import com.yufeng.service.VlogService;
import com.yufeng.utils.JsonUtils;
import com.yufeng.utils.PagedGridResult;
import com.yufeng.vo.CommentVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lzm
 * @CreateTime 2025年5月21日 13:37
 */
@Service
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentMapperCustom commentMapperCustom;

    @Autowired
    private VlogService vlogService;

    @Autowired
    private MsgService msgService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Sid sid;


    // 创建一条评论给当前视频、或者回复某条评论
    // 实则底层是将一个Comment的pojo对象存储到MySQL中
    @Override
    public CommentVO createComment(CommentBO commentBO) {
        // 1. 生成评论id(主键)
        String commentId = this.sid.nextShort();
        // 2. 将评论信息存储到MySQL中
        Comment comment = new Comment();
        comment.setId(commentId); // 当前评论主键(唯一性)

        comment.setVlogId(commentBO.getVlogId()); // 评论那个视频的id
        comment.setVlogerId(commentBO.getVlogerId()); // 评论视频是哪个作者（vloger）的关联id

        comment.setCommentUserId(commentBO.getCommentUserId()); // 评论人id
        comment.setFatherCommentId(commentBO.getFatherCommentId()); // 如果是回复留言；如果不是，则这个默认为0

        comment.setContent(commentBO.getContent()); // 最后最重要的，设置评论内容

        comment.setLikeCounts(0); // 设置该评论默认的点赞数为0
        comment.setCreateTime(new Date());

        // 3. 执行插入操作
        commentMapper.insert(comment);

        // 4. Redis操作,当前视频的评论总数累加1
        redis.increment(REDIS_VLOG_COMMENT_COUNTS + ":" + commentBO.getVlogId(), 1);


        // 5. 当用户发表一条评论的时候(也就是向数据库中插入一条评论记录)，就立刻返还一个VO给前端。
        // 这里用到‘弱一致性’，也就是传统来说，我们应该在数据库中查询出这条视频的所有评论并分页，然后再返回给前端。但现在不这么做，前端维护了一个List，我们只需要将这条评论发给前端并添加到List中的最上面即可。当用户再次点击评论列表的时候，前端则会去请求服务器获取最新的评论列表，这时候再返还一个最新的List给前端。这样就避免了频繁的数据库查询操作，提升了性能。
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO); // 将comment对象中的属性复制到commentVO对象中


        // 系统消息
        // 6. 发送系统消息给视频作者、父评论者，告诉他有用户评论了他的视频
        // 6.1 判断是普通的评论，还是回复评论(type)
        String type = MessageEnum.COMMENT_VLOG.enValue; // 默认是评论视频
        if (StringUtils.isNotBlank(commentBO.getFatherCommentId()) && !commentBO.getFatherCommentId().equalsIgnoreCase("0")) { // 代表这条记录是回复评论
            type = MessageEnum.REPLY_YOU.enValue; // 回复评论
        }
        // 6.2 msgContent
        Map<String, String> msgContent = new HashMap<>();
        msgContent.put("commentId", commentId); // 评论的id
        msgContent.put("commentContent", commentBO.getContent()); // 评论的内容
        // 获取视频的封面、id
        Vlog vlog = vlogService.getVlog(commentBO.getVlogId());
        msgContent.put("vlogId", vlog.getId()); // 视频的id
        msgContent.put("vlogCover", vlog.getCover()); // 视频的封面


        // msgService.createMsg(commentBO.getCommentUserId(), commentBO.getVlogerId(), type, msgContent);


        // 发送信息给RabbitMQ
        // 6.3 创建一个MO给RabbitMQ作为消息发送
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(commentBO.getCommentUserId()); // 设置发送者ID
        messageMO.setToUserId(commentBO.getVlogerId()); // 设置接收者ID
        messageMO.setMsgContent(msgContent); // 设置消息内容
        // 6.4 交给RabbitMQ生产消息(异步执行，不会阻塞当前线程)
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG, // 要发送交换机的名称
                "sys.msg." + type, // 路由键(规定消息的路由规则，也就是交换机将消息发送到哪个队列)
                JsonUtils.objectToJson(messageMO)); // 消息具体内容

        return commentVO;
    }



    // 查询当前视频的评论列表
    @Override
    public List<CommentVO> getCommentList(String vlogId, String userId, Integer page, Integer pageSize) {
        // 把查询条件封装到map中
        Map<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);

        // 开启分页
        PageHelper.startPage(page, pageSize);

        // 执行dao层查询
        List<CommentVO> commentList = commentMapperCustom.getCommentList(map);

        // 需要循环每一个vo，因为需要知道每个评论的点赞总数
        for (CommentVO commentVO : commentList) {
            String commentId = commentVO.getCommentId();
            // 1. 查询某个评论的点赞数
            String countsStr = redis.getHashValue(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId);
            Integer counts = 0;
            if (StringUtils.isNotBlank(countsStr)) {
                counts = Integer.valueOf(countsStr);
            }
            commentVO.setLikeCounts(counts); // 设置当前评论的点赞数

            // 2. 查询当前用户是否点赞了该评论
            String doILike = redis.hget(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId);
            if (StringUtils.isNotBlank(doILike) && doILike.equalsIgnoreCase("1")) {
                commentVO.setIsLike(1); // 设置当前用户点赞了该评论
            }
        }

        return commentList;
    }



    // 删除评论
    @Override
    public void deleteComment(String commentUserId,
                              String commentId,
                              String vlogId) {
        Comment pendingDelete = new Comment();
        pendingDelete.setCommentUserId(commentUserId); // 评论人的id
        pendingDelete.setId(commentId); // 评论的id，这是主键，所以可以直接删除

        // 直接调用dao层删除评论
        commentMapper.delete(pendingDelete);

        // 当前的评论数在Redis中减1
        redis.decrement(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId, 1);
    }

    // 通过主键查询某个评论的pojo类(可以获得视频发布者id、评论者id、视频的id、评论内容)
    @Override
    public Comment getComment(String id) {
        return commentMapper.selectByPrimaryKey(id);
    }


}
