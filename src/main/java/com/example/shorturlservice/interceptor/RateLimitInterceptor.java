package com.example.shorturlservice.interceptor;

import com.example.shorturlservice.annotation.RateLimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 如果拦截到的不是一个方法（比如拦截到了静态网页或图片），直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 2. 检查这个方法头上有没有贴咱们刚才发明的 @RateLimit 标签
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

        // 3. 没贴标签？说明这个接口不需要保护，直接放行！
        if (rateLimit == null) {
            return true;
        }

        // 4. 贴了标签！提取出规则：几秒内，允许访问几次？
        int time = rateLimit.time();
        int count = rateLimit.count();

        // 5. 获取访客的真实 IP 地址
        String ip = getIpAddress(request);
        // 拼接成 Redis 里的专属记账 Key，例如："rate_limit:192.168.1.100:/api/short-url/generate"
        String redisKey = "rate_limit:" + ip + ":" + request.getRequestURI();

        // 6. 去 Redis 里查查他之前访问过几次
        String currentCountStr = redisTemplate.opsForValue().get(redisKey);

        if (currentCountStr == null) {
            // 第一次来，给他记上 1 次，并设置这个记账本过几秒（time）就自动销毁
            redisTemplate.opsForValue().set(redisKey, "1", time, TimeUnit.SECONDS);
            return true;
        }

        int currentCount = Integer.parseInt(currentCountStr);
        if (currentCount < count) {
            // 还没超标，次数 +1，放行！
            redisTemplate.opsForValue().increment(redisKey);
            return true;
        } else {
            // 🚨 7. 超过次数限制了！直接拦死，返回报错信息！
            response.setContentType("application/json;charset=UTF-8");

            // 🌟 核心修改：我们不依赖第三方工具包了，直接纯手工拼接一段标准的 JSON 字符串发给前端！
            String jsonResult = "{\"code\": 429, \"message\": \"小伙子手速太快啦！请 " + time + " 秒后再试。\"}";

            response.getWriter().write(jsonResult);
            return false; // 返回 false 就是告诉 Spring：门焊死了，绝对不让进！
        }
    }

    // 获取真实 IP 的辅助方法（大厂必备，防止别人套了一层代理来伪装 IP）
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果有多个代理，第一个 IP 就是真实的客户端 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        return ip;
    }
}