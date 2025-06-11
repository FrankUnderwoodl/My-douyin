package com.yufeng.service;


import com.yufeng.bo.VlogBO;
import com.yufeng.pojo.Vlog;
import com.yufeng.utils.PagedGridResult;
import com.yufeng.vo.IndexVlogVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Lzm
 * @CreateTime 2025年5月09日 21:42
 */
public interface VlogService {

    /**
     * 向数据库中存入一条视频记录
     */
    public void createVlog(VlogBO vlogBO);


    /**
     * 用于展示App首页、搜索页的Vlog列表
     */
    public PagedGridResult getIndexVlogList(String userId,
                                            String search,
                                            Integer page,
                                            Integer pageSize);


    /**
     * 根据视频主键查询某个vlog
     * 用户点击某个视频的时候，查询这个视频的详细信息(不含tab)
     */
    public IndexVlogVO getVlogDetailById(String userId,String vlogId);



    /**
     * 用户把视频改为公开/私密
     */
    public void changeToPrivateOrPublish(String  userId,
                                         String vlogerId,
                                         Integer yesOrNo);

    /**
     * 查询当前用户的短视频列表(公开、私密)
     */
    public PagedGridResult queryMyVlogList(String userId,
                                           Integer page,
                                           Integer pageSize,
                                           Integer yesOrNo);


    /**
     * 用户点赞/喜欢视频
     */
    public void userLikeVlog(String userId, String vlogId);


    /**
     * 把count刷入MySQL，其实就是将Redis里的点赞数持久化到MySQL
     */
    public void flushCountsToMySQL(String vlogId, Integer counts);


    /**
     * 通过主键id查询某个视频
     */
    public Vlog getVlog(String id);



    /**
     * 用户取消点赞/喜欢视频
     */
    public void userUnLikeVlog(String userId, String vlogId);



    /**
     * 查询当前视频的点赞数
     */
    public Integer getVlogBeLikedCounts(String vlogId);



    /**
     * 查询当前用户点赞过的视频列表
     */
    public PagedGridResult getMyLikedVlogList(String userId,
                                           Integer page,
                                           Integer pageSize);


    /**
     * 查询当前用户关注博主发布的视频列表
     */
    public PagedGridResult getMyFollowVlogList(String myId,
                                              Integer page,
                                              Integer pageSize);


    /**
     * 查询当前用户朋友所发布的视频列表
     */
    public PagedGridResult getMyFriendVlogList(String myId,
                                               Integer page,
                                               Integer pageSize);


}
