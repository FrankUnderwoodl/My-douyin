package com.yufeng.config;

import com.yufeng.base.RabbitMQConfig;
import com.yufeng.enums.MessageEnum;
import com.yufeng.exceptions.GraceException;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.mo.MessageMO;
import com.yufeng.service.MsgService;
import com.yufeng.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Lzm
 */
@Slf4j
@Component
public class RabbitMQConsumer {

    @Autowired
    MsgService msgService;

    /**
     * 这里可以定义消费者相关的配置，比如监听队列、处理消息等
     * 目前这个类是空的，具体实现可以根据需要添加
     * 注意：RabbitMQ的消费者通常会在其他类中实现，比如使用@RabbitListener注解来监听队列消息
     * 这里可以添加相关的消费者逻辑代码
     */


    // @RabbitListener注解的作用是： 监听指定的RabbitMQ队列，当有消息到达时，会调用对应的方法来处理消息。
    // queue参数的作用是: 指定要监听的队列名称
    @RabbitListener(queues = RabbitMQConfig.QUEUE_SYS_MSG)
    public void watchQueue(String payload, Message message) {
        // 这里是监听队列的逻辑
        log.info("收到消息: {}", payload);

        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        log.info("收到的路由键: {}", routingKey);

        // 通过字符换获取mo对象
        MessageMO messageMO = JsonUtils.jsonToPojo(payload, MessageMO.class);

        // 这里就可以通过routingKey来判断消息类型
        if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.FOLLOW_YOU.enValue)) { // 关注博主
            msgService.createMsg(messageMO.getFromUserId(),
                                 messageMO.getToUserId(),
                                 MessageEnum.FOLLOW_YOU.type,
                                null);

        } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.LIKE_VLOG.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.LIKE_VLOG.type,
                    messageMO.getMsgContent());

        } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.COMMENT_VLOG.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.COMMENT_VLOG.type,
                    messageMO.getMsgContent());

        } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.REPLY_YOU.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.REPLY_YOU.type,
                    messageMO.getMsgContent());

        } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.LIKE_COMMENT.enValue)){
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.LIKE_COMMENT.type,
                    messageMO.getMsgContent());

        } else {
            // 如果没有匹配到任何路由键，则抛出异常
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }

    }
}
