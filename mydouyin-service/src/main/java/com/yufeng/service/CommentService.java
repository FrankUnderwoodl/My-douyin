package com.yufeng.service;

import com.yufeng.bo.CommentBO;
import com.yufeng.pojo.Comment;
import com.yufeng.utils.PagedGridResult;
import com.yufeng.vo.CommentVO;

import java.util.List;

/**
 * @author Lzm
 * @CreateTime 2025年5月21日 13:13:59
 */
public interface CommentService {

    /**
     * 发表评论(也就是把你写的评论内容存到数据库中)
     */
    public CommentVO createComment(CommentBO commentBO);



    /**
     * 查询当前视频的评论列表
     */
    public List<CommentVO> getCommentList(String vlogId,
                                          String userId,
                                          Integer page,
                                          Integer pageSize);



    /**
     * 删除评论
     */
    public void deleteComment(String commentUserId,  // 评论人的id
                              String commentId, // 评论的id
                              String vlogId); // 视频的id



    /**
     * 通过主键查询某个评论的pojo类(视频发布者id、评论者id、视频的id、评论内容)
     */
    public Comment getComment(String id);

}
