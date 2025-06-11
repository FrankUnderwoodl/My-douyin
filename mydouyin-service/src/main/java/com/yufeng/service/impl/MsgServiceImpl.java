package com.yufeng.service.impl;

import com.yufeng.base.BaseInfoProperties;
import com.yufeng.enums.MessageEnum;
import com.yufeng.mo.MessageMO;
import com.yufeng.pojo.Users;
import com.yufeng.repository.MessageRepository;
import com.yufeng.service.MsgService;
import com.yufeng.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Lzm
 * @CreateTime 2025年5月24日 21:57
 */
@Service
public class MsgServiceImpl extends BaseInfoProperties implements MsgService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;


    // 创建一条消息(实则底层是将一个MessageMO对象存储到MongoDB中，通过fromUserId、toUserId、msgType等参数就能够创建一个mo给MongoDB)
    @Override
    public void createMsg(String fromUserId, String toUserId, Integer msgType, Map msgContent) {
        // 通过service获取发送者用户信息(其实就是从MySQL中的user表获取昵称、头像等信息),用于展示给当前用户
        Users fromUser = userService.getUser(fromUserId);

        // 创建一个消息对象(存在MongoDB中，其实就是一个json对象)
        MessageMO messageMO = new MessageMO();
        // messageMO.setId(); // 这里不需要手动设置ID，MongoDB会自动生成
        // 设置发送者用户ID
        messageMO.setFromUserId(fromUserId);
        // 设置发送者昵称，可以从MySQL中user表获取
        messageMO.setFromNickName(fromUser.getNickname());
        // 设置发送者头像，可以从MySQL中的user表获取
        messageMO.setFromFace(fromUser.getFace());
        // 设置接收者用户ID
        messageMO.setToUserId(toUserId);
        // 设置消息类型，例如关注消息、评论消息等
        messageMO.setMsgType(msgType);
        if (msgContent != null) { // 如果参数msgContent不为空，则设置消息内容
            messageMO.setMsgContent(msgContent); // 如果content不为空，则设置消息内容
        }
        messageMO.setCreateTime(new java.util.Date()); // 设置当前时间为消息创建时间


        // 保存消息到MongoDB(底层其实就是dao(Data Access Object)层)
        messageRepository.save(messageMO);
    }


    /**
     * 查询当前用户的消息列表
     * 有两种方式去查：
     * ①messageRepository.findAll()，构建一个example，然后把MessageMO的toUserId作为查询条件
     * ②messageRepository.findAllByToUserIdOrderByCreateTimeDesc(toUserId, pageRequest)，这种方式是更简洁的，直接通过方法名来查询，Spring Data MongoDB会自动解析方法名并生成相应的查询语句。
     */
    @Override
    public List<MessageMO> queryList(String toUserId, Integer page, Integer pageSize) {
        // 这句代码的作用是创建一个分页请求对象，指定页码和每页大小，底层原理是你的代码第64行调用 findAllByToUserIdOrderByCreateTimeDesc 方法时，这个 pageRequest 对象会被 Spring Data 处理，转换为相应的 MongoDB 查询语句，实现分页查询功能。
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.Direction.DESC, "createTime");

        List<MessageMO> list = messageRepository.findAllByToUserIdOrderByCreateTimeDesc(toUserId, pageRequest);

        // 接下来循环每一个消息对象，可能需要进行一些额外的处理，比如前端需要标记互粉状态等
        for (MessageMO mo : list) {
            // 如果消息类型是关注消息，则需要查询我之前有没有关注过他，用于在前端标记“互粉”“互关”
            if (mo.getMsgType() != null && mo.getMsgType() == MessageEnum.FOLLOW_YOU.type) {
                Map map = mo.getMsgContent();
                if (map == null) {
                    map = Collections.emptyMap(); // 如果消息内容为空，初始化为一个空的Map
                }
                // 判断用户接收者是否有关注过发送者
                String relationship = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + mo.getToUserId() + ":" + mo.getFromUserId()); // 判断当前用户是否有关注过视频发布者
                if (relationship != null && relationship.equalsIgnoreCase("1")) {
                    // 如果关系存在，说明互相关注
                    map.put("isFriend", true);
                } else {
                    // 如果关系不存在，说明没有互相关注
                    map.put("isFriend", false);
                }
                mo.setMsgContent(map); // 更新消息内容
            }
        }
        return list;
    }
}
