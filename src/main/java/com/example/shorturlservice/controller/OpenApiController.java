package com.example.shorturlservice.controller;

import com.example.shorturlservice.entity.ShortUrl;
import com.example.shorturlservice.service.ShortUrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/open") // 🌟 专属前缀，受 ApiAuthInterceptor 严格保护
public class OpenApiController {

    @Autowired
    private ShortUrlService shortUrlService;

    // 外部系统专用的生成短链接口
    // 外部系统专用的生成短链接口
    @PostMapping("/generate")
    public Map<String, Object> generateShortUrl(
            @RequestBody Map<String, String> requestData,
            // 🌟 核心新增：用 @RequestAttribute 优雅地接过保安递来的 appId
            @RequestAttribute("appId") Long appId) {

        String originalUrl = requestData.get("originalUrl");

        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("message", "originalUrl 不能为空");
            return error;
        }

        // 创建短链实体
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setOriginalUrl(originalUrl);
        shortUrl.setName("OpenAPI外部调用生成");

        // 🌟 核心新增：把这个短链和外部客户绑定！以后就可以按应用收费了！
        shortUrl.setAppId(appId);

        // 调用核心 Service
        ShortUrl savedUrl = shortUrlService.create(shortUrl, true);

        // 封装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "API生成成功");
        result.put("data", savedUrl);

        return result;
    }
}