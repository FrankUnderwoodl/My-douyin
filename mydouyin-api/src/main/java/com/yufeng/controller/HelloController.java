package com.yufeng.controller;

import com.yufeng.base.RabbitMQConfig;
import com.yufeng.grace.result.GraceJSONResult;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Lzm
 * @CreateTime 2025年5月05日 18:05
 */

@Slf4j
@RestController
@Api(tags = "HelloController 相关业务功能的接口") // 这个注解可以用来定义接口类的名字
@RequestMapping("hello") // 这个注解可以用来定义接口类的请求路径
// 这个注解是SpringCloud的原生注解，用来实现配置中心的动态刷新(其实就是在配置中心修改了配置后，自动刷新到当前应用中。这里的配置中心具体指的是Nacos)
@RefreshScope
public class HelloController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${nacos.counts}")
    private Integer counts; // 这个值会从Nacos配置中心获取

    @GetMapping("getCounts")
    public Object getCounts() {
        log.info("HelloController getCounts方法被调用了！");

        // 这个值会从Nacos配置中心获取
        log.info("从Nacos配置中心获取的counts值为: {}", counts);

        return GraceJSONResult.ok(counts);
    }

    @GetMapping("produce")
    public Object produce(){
        log.info("HelloController produce方法被调用了！");

        // 这个convertAndSend是异步执行的
        /**
         * 队列queue_sys_msg绑定到交换机exchange_msg时使用的路由模式是sys.msg.*
         * 这意味着只有路由键以sys.msg.开头且后面只跟一个单词的消息才会被路由到该队列
         * sys.msg.send完全符合这个模式，所以消息会被成功发送到这个队列
         */
        rabbitTemplate.convertAndSend(
                            RabbitMQConfig.EXCHANGE_MSG, // 要发送给的交换机名称
                            "sys.msg.send",   // 路由键(规定消息的路由规则，也就是交换机将消息发送到哪个队列)
                            "我发送了一条信息"); // 消息具体内容

        return GraceJSONResult.ok();
    }


    @GetMapping("produce2")
    public Object produce2(){
        log.info("HelloController produce2方法被调用了！");

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG, // 要发送给的交换机名称
                "sys.msg.delete",   // 路由键(规定消息的路由规则，也就是交换机将消息发送到哪个队列)
                "我删除了一条记录"); // 消息具体内容

        return GraceJSONResult.ok();
    }


}
