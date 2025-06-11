package com.yufeng.mo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.Map;

/**
 * @author Lzm
 * @CreateTime 2025年5月24日 01:05
 * ‘@Document注解’是Spring Data MongoDB提供的注解，用于将Java类映射到MongoDB的集合(collection)中。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document(collection = "message") // 指定MongoDB中的集合名称为"message"
public class MessageMO {

    @Id
    private String id; // 消息主键的id，@ID注解的底层作用是将该字段映射为MongoDB的ObjectId类型


    // 消息发送者的用户id
    @Field("fromUserId")
    private String fromUserId;

    // 消息发送者的昵称
    @Field("fromUserName")
    private String fromNickName;

    // 消息发送者的头像
    @Field("fromFace")
    private String fromFace;


    // 消息接收者的用户id
    @Field("toUserId")
    private String toUserId;


    // 消息类型，例如文本消息、图片消息、视频消息等
    @Field("msgType")
    private Integer msgType;

    // 消息内容，例如内容或图片/视频的URL
    @Field("msgContent")
    private Map msgContent;

    // 消息创建时间戳，单位为毫秒
    @Field("createTime")
    private Date createTime;

}
