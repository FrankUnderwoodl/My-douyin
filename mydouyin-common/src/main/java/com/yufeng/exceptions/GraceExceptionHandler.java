package com.yufeng.exceptions;

import com.yufeng.grace.result.GraceJSONResult;
import com.yufeng.grace.result.ResponseStatusEnum;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一异常拦截处理
 * 可以针对所有的异常类型进行捕获(不用写try...、throw了)，然后返回json信息到前端
 * @author Lzm
 * '@ControllerAdvice' 注解是 Spring MVC 中非常重要的一个注解，它的作用不仅仅是加入 IOC 容器，还包括以下几个关键功能：
 * 全局异常处理：它允许你在一个地方统一处理来自所有 Controller 的异常，正如你代码中所做的那样
 * AOP 切面：本质上是 Controller 层面的切面，能够对 Controller 中的方法进行拦截和处理
 * 作用域：默认情况下，它会应用到应用程序中所有的 Controller 上，你也可以通过配置让它只作用于特定的 Controller
 */
@ControllerAdvice
public class GraceExceptionHandler {

    // @ExceptionHandler(MyCustomException.class)这个注解的意思是专门处理MyCustomException这个异常,随后进入到returnMyException函数,将一个json抛给前端
    @ExceptionHandler(MyCustomException.class)
    @ResponseBody
    public GraceJSONResult returnMyException(MyCustomException e) {
        e.printStackTrace();
        return GraceJSONResult.exception(e.getResponseStatusEnum());
    }


    // 问: 怎么理解这个异常处理器的作用？
    // 答: 这个异常处理器的作用是捕获所有的MethodArgumentNotValidException异常(也就是BindingResult result)，并返回一个GraceJSONResult对象给前端(@ResponseBody的加持下)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public GraceJSONResult returnMethodArgumentNotValid(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        Map<String, String> map = getErrors(result);
        return GraceJSONResult.errorMap(map);
    }



    // 最大上传文件大小异常处理器
    // 也就是说，如果上传的文件超过了2MB，就会抛出MaxUploadSizeExceededException异常，然后进入到这个方法中
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public GraceJSONResult returnMaxUploadSize(MaxUploadSizeExceededException e) {
//        e.printStackTrace();
        return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_MAX_SIZE_2MB_ERROR);
    }

    public Map<String, String> getErrors(BindingResult result) {
        Map<String, String> map = new HashMap<>();
        List<FieldError> errorList = result.getFieldErrors();
        for (FieldError ff : errorList) {
            // 错误所对应的属性字段名
            String field = ff.getField();
            // 错误的信息
            String msg = ff.getDefaultMessage();
            map.put(field, msg);
        }
        return map;
    }
}
