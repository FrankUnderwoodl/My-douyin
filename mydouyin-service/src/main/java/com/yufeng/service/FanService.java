package com.yufeng.service;

import com.yufeng.utils.PagedGridResult;

/**
 * @author Lzm
 * @CreateTime 2025年5月11日 13:44
 */
public interface FanService {


    /**
     * 目的是:关注当前视频的博主
     *
     * @param myId  我的id
     * @param vlogerId 博主id
     */
    public void doFollow(String myId, String vlogerId);


    /**
     * 目的是:取消关注当前视频的博主
     *
     * @param myId  我的id
     * @param vlogerId 博主id
     */
    public void doCancel(String myId, String vlogerId);


    /**
     * 目的是:查询当前用户是否关注了该视频博主
     *
     * @param myId  我的id
     * @param vlogerId 博主id
     */
    public boolean queryDoIFollowVloger(String myId, String vlogerId);


    /**
     * 查询当前用户关注的视频博主列表
     */
    public PagedGridResult queryMyFollows(String myId,
                                           Integer page,
                                           Integer pageSize);

    /**
     * 查询当前用户的粉丝列表
     */
    public PagedGridResult queryMyFans(String myId,
                                        Integer page,
                                        Integer pageSize);

}
