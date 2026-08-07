package com.winforrd.taskflow.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> cn.dev33.satoken.stp.StpUtil.isLogin()))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login",
                                    "/api/auth/register");
    }
}
