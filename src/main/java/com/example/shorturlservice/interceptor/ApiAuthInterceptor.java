package com.example.shorturlservice.interceptor;

import com.example.shorturlservice.entity.ApiClient;
import com.example.shorturlservice.repository.ApiClientRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private ApiClientRepository apiClientRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头 (Headers) 中获取三大件
        String appKey = request.getHeader("X-App-Key");     // 外部应用的 AK
        String timestamp = request.getHeader("X-Timestamp");// 发起请求的时间戳
        String sign = request.getHeader("X-Sign");          // 外部应用计算好的签名

        // 2. 检查参数是否完整
        if (appKey == null || timestamp == null || sign == null) {
            return reject(response, "非法请求：缺少必备的 API 鉴权请求头(X-App-Key, X-Timestamp, X-Sign)");
        }

        // 3. 防重放攻击：校验时间戳 (假设请求超过 5 分钟就认为过期，防止黑客拿着旧请求反复刷)
        try {
            long reqTime = Long.parseLong(timestamp);
            if (System.currentTimeMillis() - reqTime > 5 * 60 * 1000) {
                return reject(response, "请求已过期，请校准客户端时间");
            }
        } catch (NumberFormatException e) {
            return reject(response, "时间戳格式错误");
        }

        // 4. 去数据库核对这个 AK 存不存在，或者是不是被禁用了
        ApiClient client = apiClientRepository.findByAccessKey(appKey);
        if (client == null || !client.getEnabled()) {
            return reject(response, "AppKey 无效或已被后台禁用");
        }

        // 🌟 5. 最核心的验签逻辑！
        // 我们的加密配方：MD5(appKey + timestamp + 数据库里查到的 secretKey)
        String rawStr = appKey + timestamp + client.getSecretKey();
        // 使用 Spring Boot 自带的 MD5 工具类直接加密
        String expectedSign = DigestUtils.md5DigestAsHex(rawStr.getBytes());

        // 6. 核对签名
        if (!expectedSign.equalsIgnoreCase(sign)) {
            System.out.println("🚨 发现伪造请求！预期签名: " + expectedSign + ", 实际收到: " + sign);
            return reject(response, "签名(Sign)校验失败，存在数据篡改风险");
        }
        request.setAttribute("appId", client.getId());

        // 走到这里，说明身份合法，签名无误，放行！
        return true;
    }

    // 辅助方法：给黑客返回无情的报错 JSON
    private boolean reject(HttpServletResponse response, String msg) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\": 401, \"message\": \"" + msg + "\"}");
        return false;
    }
}