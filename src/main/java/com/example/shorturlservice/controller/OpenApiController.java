package com.example.shorturlservice.controller;

import com.example.shorturlservice.entity.ApiClient;
import com.example.shorturlservice.entity.ShortUrl;
import com.example.shorturlservice.repository.ApiClientRepository;
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

    // 🌟 新增：注入 ApiClientRepository，用来查客户的真实名字
    @Autowired
    private ApiClientRepository apiClientRepository;

    // 外部系统专用的生成短链接口
    @PostMapping("/generate")
    public Map<String, Object> generateShortUrl(
            @RequestBody Map<String, String> requestData,
            // 🌟 优雅地接过保安递来的 appId
            @RequestAttribute("appId") Long appId) {

        String originalUrl = requestData.get("originalUrl");

        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("message", "originalUrl 不能为空");
            return error;
        }

        // 🌟 核心新增逻辑：根据 appId，去数据库查出真实的 appName
        // .orElse(null) 意思是如果万一没查到，就返回 null，防止报错
        ApiClient client = apiClientRepository.findById(appId).orElse(null);
        // 如果查到了客户，并且客户有名字，就用客户的名字；否则给个默认的“未知应用”
        String finalName = (client != null && client.getAppName() != null) ? client.getAppName() : "未知应用";


        // 创建短链实体
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setOriginalUrl(originalUrl);

        // 🌟 核心修改：不再写死，直接填入查出来的真实应用名称！
        shortUrl.setName(finalName);

        // 把这个短链和外部客户绑定！以后就可以按应用收费了！
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