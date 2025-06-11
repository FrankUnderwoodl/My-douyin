package com.yufeng.config;

import com.yufeng.intercepter.PassportInterceptor;
// import com.yufeng.intercepter.UserTokenInterceptor;
import com.yufeng.intercepter.UserTokenInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Lzm
 * 作用:注册拦截器
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    // 注册一个passportInterceptor(IP限制)拦截器
    @Bean
    public PassportInterceptor passportInterceptor() {
        return new PassportInterceptor();
    }

    // 注册一个修改用户信息的拦截器(登陆限制:只能一个用户修改信息)
    @Bean
    public UserTokenInterceptor userTokenInterceptor() {
        return new UserTokenInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 将passportInterceptor拦截器(IP限制)装到/passport/getSMSCode这个路径上
        registry.addInterceptor(passportInterceptor())
                .addPathPatterns("/passport/getSMSCode");

        // 将userTokenInterceptor拦截器装到/userInfo/modifyUserInfo和/userInfo/modifyImage这个路径上
        registry.addInterceptor(userTokenInterceptor())
                .addPathPatterns("/userInfo/modifyUserInfo")
                .addPathPatterns("/userInfo/modifyImage");
    }


}
