package com.example.shorturlservice.config;

import com.example.shorturlservice.interceptor.ApiAuthInterceptor;
import com.example.shorturlservice.interceptor.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Autowired // 🌟 把刚写好的 API 保安请过来
    private ApiAuthInterceptor apiAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 防刷保安：拦截所有接口
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/**");

        // 🌟 2. AK/SK 验签保安：【只拦截】专门给外部应用开的 /api/open 开头的接口
        registry.addInterceptor(apiAuthInterceptor).addPathPatterns("/api/open/**");
    }
}