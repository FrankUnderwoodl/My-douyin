package com.yufeng.exceptions;

import com.yufeng.grace.result.ResponseStatusEnum;

/**
 * 优雅地处理异常，统一封装(实则就是不用手动写throw new Exception)
 * @author Lzm
 */
public class GraceException {

    public static void display(ResponseStatusEnum responseStatusEnum) {
        throw new MyCustomException(responseStatusEnum);
    }

}
