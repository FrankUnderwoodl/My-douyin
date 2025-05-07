package com.yufeng.intercepter;

import com.yufeng.controller.BaseInfoProperties;
import com.yufeng.exceptions.GraceException;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Lzm
 * @CreateTime
 * 这个类是用来拦截请求的，主要是为了限制用户在60秒之内只能获得一次验证码2025年5月07日 01:15
 */
@Slf4j
public class PassportInterceptor extends BaseInfoProperties implements HandlerInterceptor {

    /**
     * 作用:每当请求到达Controller之前，都会执行这个方法
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        // 获取用户的ip
        String userIp = IPUtil.getRequestIp(request);
        // 判断用户ip是否存在
        boolean keyIsExist = redis.keyIsExist(MOBILE_SMSCODE + ":" + userIp);
        // 如果存在，说明用户在60秒之内已经获得过验证码了
        if (keyIsExist) {
            // 提示信息
            // log.info("短信发送频率太大！");  // 这里可以优化，将异常信息传给前端
            GraceException.display(ResponseStatusEnum.SMS_NEED_WAIT_ERROR);
            // 返回错误信息，禁止用户继续请求
            return false;
        }
        return true;
    }

    /**
     * 作用:请求从Controller返回之后，视图解析器之前，执行这个方法
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    /**
     * 作用:请求完成之后，执行这个方法
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
