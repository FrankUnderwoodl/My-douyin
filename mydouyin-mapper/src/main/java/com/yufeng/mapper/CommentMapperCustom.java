package com.yufeng.mapper;

import com.yufeng.my.mapper.MyMapper;
import com.yufeng.pojo.Comment;
import com.yufeng.vo.CommentVO;
import com.yufeng.vo.FansVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author Lzm
 */
@Repository
public interface CommentMapperCustom {

    /**
     * 查询当前视频的评论列表
     */
    public List<CommentVO> getCommentList(@Param("paramMap") Map<String,Object> map);
}