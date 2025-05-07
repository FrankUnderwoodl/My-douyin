package com.yufeng.controller;

import com.yufeng.grace.result.GraceJSONResult;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Lzm
 * @CreateTime 2025年5月05日 18:05
 */

@Slf4j
@RestController
@Api(tags = "HelloController") // 这个注解可以用来定义接口类的名字
public class HellorController {

    @GetMapping("/hello")
    public Object hello() {
        log.info("HelloController.hello");
        return GraceJSONResult.ok("Hello, World! 🎉");
    }

    @GetMapping("/test")
    public Object test() {
        return "Test endpoint!";
    }
}
