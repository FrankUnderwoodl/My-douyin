// package com.yufeng.config;
//
// import org.springframework.amqp.core.Binding;
// import org.springframework.amqp.core.BindingBuilder;
// import org.springframework.amqp.core.ExchangeBuilder;
// import org.springframework.amqp.core.Queue;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
//
// /**
//  * @author Lzm
//  */
// @Configuration
// public class RabbitMQConfig {
//     /**
//      * 根据模型编写代码
//      * 1.定义交换机
//      * 2.定义队列
//      * 3.创建交换机
//      * 4.创建队列
//      * 5.绑定交换机和队列
//      */
//
//     // 1.定义交换机名称(短视频交换机)
//     public static final String EXCHANGE_MSG = "exchange_msg";
//
//     // 2.定义队列名称(短视频队列)
//     public static final String QUEUE_SYS_MSG = "queue_sys_msg";
//
//     // 3.创建交换机(实则就是一个交换机的Bean)，type是交换机的类型，这里使用的是主题交换机（topic exchange），可以根据需要选择其他类型的交换机，如direct、fanout等。
//     @Bean
//     public org.springframework.amqp.core.Exchange exchangeMsg() {
//         return ExchangeBuilder                             // 构建交换机
//                 .topicExchange(EXCHANGE_MSG)               // 指定交换机类型为主题交换机，参数是交换机名称
//                 .durable(true)                    // 设置交换机持久化(意思是重启后交换机依然存在)
//                 .build();                                  // 构建交换机对象
//     }
//
//     // 4.创建队列(实则就是一个队列的Bean)
//     @Bean
//     public Queue queueSysMsg() {
//         return new Queue(QUEUE_SYS_MSG, true); // 队列名称和持久化设置(默认是持久化的)
//     }
//
//     /**
//      * 5.绑定交换机和队列
//      * 理解:有很多的队列，比如存储系统消息的、存储评论消息的、存储点赞消息的等等，我想发一个api同时给系统消息队列和评论消息队列，
//      * 那么我就需要一个交换机来做这个事情。
//      */
//     @Bean
//     public Binding bindingSysMsg() {
//         return BindingBuilder
//                 .bind(queueSysMsg()) // 绑定队列
//                 .to(exchangeMsg())   // 绑定交换机
//                 .with("sys.msg.*")     // 路由键(虽然这里只定义了一个key，但实际上生产者可以发送多个不同的key，比如 sys.msg.login、sys.msg.logout 等) 底层理解: 这就是所谓的路由规则，生产者发布信息的时候，只有使用sys.msg.*，才会被QUEUE_SYS_MSG这个队列所接收
//                 .noargs();           // 不需要额外参数
//
//         // FIXME: *和#的区别是什么？
//         // *表示匹配一个词，#表示匹配零个或多个词，举个例子:sys.msg.* 能匹配：sys.msg.login、sys.msg.logout, sys.msg.# 能匹配：sys.msg、sys.msg.login、sys.msg.user.login.success
//     }
//
//
//     // 另一种方式绑定交换机和队列
//     // @Bean
//     // public Binding binding(@Qualifier("EXCHANGE_MSG") Exchange exchange,
//     //                        @Qualifier("QUEUE_SYS_MSG") Queue queue) {
//     //     return BindingBuilder
//     //             .bind(queue)             // 直接使用注入的队列参数
//     //             .to(exchange)            // 直接使用注入的交换机参数
//     //             .with("sys.msg.*")
//     //             .noargs();
//     // }
//
//
// }
