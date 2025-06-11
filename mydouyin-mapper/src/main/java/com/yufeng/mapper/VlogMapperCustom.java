package com.yufeng.mapper;

import com.yufeng.my.mapper.MyMapper;
import com.yufeng.pojo.Vlog;
import com.yufeng.vo.IndexVlogVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author Lzm
 */
@Repository
public interface VlogMapperCustom {

    /**
     * 宏观描述：向数据库中查询视频列表，并返回给前端(也就是你刷到的视频来源)
     * 微观描述：根据用户id和分页参数，查询出视频列表(视频id、作者id、视频封面、视频描述、点赞数、评论数、分享数、是否点赞)，有点复杂，所以需要自定义的SQL
     */
    public List<IndexVlogVO> getIndexVlogList(@Param("paramMap")Map<String, Object> map);


    /**
     * 宏观描述：用户点击某个视频的时候，查询这个视频的详细信息(不含tab)
     */
    public List<IndexVlogVO> getVlogDetailById(@Param("paramMap")Map<String, Object> map);


    /**
     * 描述：查询出我曾经点赞过的短视频列表
     */
    public List<IndexVlogVO> getMyLikedVlogList(@Param("paramMap")Map<String, Object> map);


    /**
     * 描述：查询出我所关注的那些博主所发布的短视频列表
     */
    public List<IndexVlogVO> getMyFollowVlogList(@Param("paramMap")Map<String, Object> map);


    /**
     * 描述：查询出我朋友(与我互粉)所发布的短视频列表
     */
    public List<IndexVlogVO> getMyFriendVlogList(@Param("paramMap")Map<String, Object> map);
}