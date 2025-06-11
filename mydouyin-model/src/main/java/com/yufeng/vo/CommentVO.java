package com.yufeng.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * @author Lzm
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentVO {
    private String id; // 当前评论的id
    private String commentId; // 当前评论的id
    private String vlogerId; // 评论的视频是哪个作者（vloger）的关联id
    private String fatherCommentId; // 如果是回复留言，则本条为子留言，需要关联查询
    private String vlogId; // 评论的视频id
    private String commentUserId; // 发布评论的用户id
    private String commentUserNickname; // 评论人昵称
    private String commentUserFace; // 评论人头像
    private String content; // 评论内容
    private Integer likeCounts; // 当前平路的点赞总数
    private String replyedUserNickname; // 被回复的用户昵称
    private Date createTime; // 评论时间
    private Integer isLike = 0; // 该评论是否被点赞
}