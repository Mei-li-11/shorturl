package com.example.shorturlservice.config;

import com.example.shorturlservice.interceptor.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 让保安拦截所有的接口 ("/**")，具体放不放行，由保安在代码里看有没有 @RateLimit 标签来决定
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/**");
    }
}