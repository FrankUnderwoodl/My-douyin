package com.yufeng.exceptions;

import com.yufeng.grace.result.ResponseStatusEnum;

/**
 * 自定义异常
 * 目的：统一处理异常信息
 *      便于解耦，拦截器、service与controller 异常错误的解耦，
 *      不会被service返回的类型而限制
 * @author Lzm
 */
public class MyCustomException extends RuntimeException {

    private ResponseStatusEnum responseStatusEnum;

    // 不过需要明确一点：这个super()调用本身并没有抛出异常，它只是在构造当前异常对象。真正的异常抛出发生在使用这个类的地方，比如：throw new MyCustomException(ResponseStatusEnum.SYSTEM_ERROR);
    public MyCustomException(ResponseStatusEnum responseStatusEnum) {
        super("异常状态码为：" + responseStatusEnum.status()
                + "；具体异常信息为：" + responseStatusEnum.msg());
        this.responseStatusEnum = responseStatusEnum;
    }

    public ResponseStatusEnum getResponseStatusEnum() {
        return responseStatusEnum;
    }

    public void setResponseStatusEnum(ResponseStatusEnum responseStatusEnum) {
        this.responseStatusEnum = responseStatusEnum;
    }
}
