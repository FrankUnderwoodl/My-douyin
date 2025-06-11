package com.yufeng.service;

import com.yufeng.bo.UpdatedUserBO;
import com.yufeng.pojo.Users;

/**
 * @author Lzm
 * @CreateTime 2025年5月07日 14:07
 */
public interface UserService {

    /**
     * 判断用户是否存在，如果存在则返回用户信息；如果不存在则，
     */
    public Users queryMobileIsExist(String mobile);

    /**
     * 创建用户信息，并且返回用户对象
     */
    public Users createUser(String mobile);

    /**
     * 根据用户id查询用户信息，适用在刷新页面就能展示你的新粉丝
     */
    public Users getUser(String userId);

    /**
     * 用户信息修改，适用于你的个人中心修改用户信息
     */
    public Users UpdateUserInfo(UpdatedUserBO updatedUserBO);

    /**
     * 用户信息修改，需要判断前端传入的type
     */
    public Users UpdateUserInfo(UpdatedUserBO updatedUserBO, Integer type);

}
