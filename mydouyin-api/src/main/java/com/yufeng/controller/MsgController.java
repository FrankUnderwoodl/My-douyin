package com.yufeng.controller;

import com.yufeng.base.BaseInfoProperties;
import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.mo.MessageMO;
import com.yufeng.service.MsgService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Lzm
 * @CreateTime 2025年5月05日 18:05
 */

@Slf4j
@RestController
@Api(tags = "MsgController 相关业务功能的接口") // 这个注解可以用来定义接口类的名字
@RequestMapping("msg") // 这个注解可以用来定义接口类的请求路径
public class MsgController extends BaseInfoProperties {

    @Autowired
    private MsgService msgService;

    @GetMapping("list")
    public GraceJSONResult list(String userId, Integer page, Integer pageSize) {
        log.info("查询用户 {} 的消息列表，页码：{}，每页大小：{}", userId, page, pageSize);

        // 注意: MongoDB 从0分页，区别于MySQL从1分页
        if (page == null) {
            page = COMMON_START_PAGE_ZERO; // 默认从第0页开始
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE; // 默认每页大小
        }
        // 调用service层
        List<MessageMO> list = msgService.queryList(userId, page, pageSize);

        return GraceJSONResult.ok(list);
    }
}
