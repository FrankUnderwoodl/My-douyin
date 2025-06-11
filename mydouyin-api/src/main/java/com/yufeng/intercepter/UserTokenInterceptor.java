package com.yufeng.intercepter;

import com.yufeng.base.BaseInfoProperties;
import com.yufeng.exceptions.GraceException;
import com.yufeng.grace.result.ResponseStatusEnum;
import com.yufeng.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Lzm
 * @CreateTime 2025年5月9日14:06:13
 * @Describe 这个类的作用主要是用来:通过判断客户端传来的token，作用是限制只能一部手机或一个客户端来访问后端接口
 */
@Slf4j
public class UserTokenInterceptor extends BaseInfoProperties implements HandlerInterceptor {

    /**
     * 作用:每当请求到达Controller之前，都会执行这个方法
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        // 给你看看前端代码:
        // uni.uploadFile({
        // 							header: {
        // 								headerUserId: userId,
        // 								headerUserToken: app.getUserSessionToken()
        // 							},
        // 							url: serverUrl + "/userInfo/modifyImage?userId=" + userId + "&type=1",


        log.info("请求的ip地址是:{}", IPUtil.getRequestIp(request));

        // 从请求中的header中获取用户的id和token
        String userId = request.getHeader("headerUserId");
        String userToken = request.getHeader("headerUserToken");

        // 判空处理
        if(StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(userToken)) {
            // 通过用户id来向Redis中查询用户的token
            String redisUserToken = redis.get(REDIS_USER_TOKEN + ":" + userId);
            // 如果查询这个id对应的token为空，说明这个用户没有登录过
            if (StringUtils.isBlank(redisUserToken)) {
                // 说明这个用户没有登录过
                GraceException.display(ResponseStatusEnum.UN_LOGIN);
                return false;
            } else {
                // 如果不为空，说明这个用户登录过
                // 但是我们还要判断一下，这个token是否和前端传过来的token一致，如果不一致，就说明用户在别的地方登录了(也就是覆盖掉原先的token)
                if (!userToken.equalsIgnoreCase(redisUserToken)) {
                    // 如果不一致，说明这个用户在别的地方登录了
                    GraceException.display(ResponseStatusEnum.TICKET_INVALID);
                    return false;
                }
            }
        } else {
            // 如果用户id和token都为空，说明这个用户没有登录过，直接抛一个‘未登录’的异常json给前端
            GraceException.display(ResponseStatusEnum.UN_LOGIN);
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
