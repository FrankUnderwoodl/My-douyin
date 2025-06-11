package com.yufeng.service;

import com.yufeng.bo.UpdatedUserBO;
import com.yufeng.mo.MessageMO;
import com.yufeng.pojo.Users;

import java.util.List;
import java.util.Map;

/**
 * @author Lzm
 * @CreateTime 2025年5月07日 14:07
 */
public interface MsgService {

    /**
     * 创建消息
     */
    public void createMsg(String fromUserId,
                          String toUserId,
                          Integer msgType,
                          Map msgContent);


    /**
     * 查询当前用户的消息列表
     */
    public List<MessageMO> queryList(String toUserId,
                                     Integer page,
                                     Integer pageSize);
}
