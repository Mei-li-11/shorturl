package com.example.shorturlservice.controller;

import com.example.shorturlservice.entity.ApiClient;
import com.example.shorturlservice.entity.ShortUrl;
import com.example.shorturlservice.repository.ApiClientRepository;
import com.example.shorturlservice.repository.ShortUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/clients")
public class ApiClientController {

    @Autowired
    private ApiClientRepository apiClientRepository;

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    // 🌟 1. 新增：引入 Redis 操作工具，专门用来“踢人”
    @Autowired
    private StringRedisTemplate redisTemplate;

    // 1. 获取所有 API 客户列表
    @GetMapping
    public Map<String, Object> getAllClients() {
        List<ApiClient> list = apiClientRepository.findAll();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "获取成功");
        result.put("data", list);
        return result;
    }

    // 2. 新增 API 客户
    @PostMapping
    public Map<String, Object> createClient(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        ApiClient client = new ApiClient();
        client.setAppName(name);
        String ak = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String sk = UUID.randomUUID().toString().replace("-", "");
        client.setAccessKey(ak);
        client.setSecretKey(sk);
        client.setEnabled(true);
        apiClientRepository.save(client);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "应用创建成功");
        return result;
    }

    // 3. 单个封禁应用（级联禁用其所有短链）
    @PatchMapping("/{id}/disable")
    public Map<String, Object> disableClient(@PathVariable Long id) {
        ApiClient client = apiClientRepository.findById(id).orElse(null);
        if (client != null) {
            client.setEnabled(false); // 封禁应用本身
            apiClientRepository.save(client);

            List<ShortUrl> urls = shortUrlRepository.findByAppId(id);
            for (ShortUrl url : urls) {
                url.setEnabled(false); // 数据库状态置为禁用

                // 🌟 2. 核心修复：顺手把这个短链在 Redis 里的缓存给彻底删掉！
                redisTemplate.delete("shorturl:" + url.getShortCode());
            }
            shortUrlRepository.saveAll(urls);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "应用已封禁，关联短链已全部失效");
        return result;
    }

    // 4. 单个解封应用（级联恢复其所有短链）
    @PatchMapping("/{id}/enable")
    public Map<String, Object> enableClient(@PathVariable Long id) {
        ApiClient client = apiClientRepository.findById(id).orElse(null);
        if (client != null) {
            client.setEnabled(true); // 解封应用本身
            apiClientRepository.save(client);

            List<ShortUrl> urls = shortUrlRepository.findByAppId(id);
            for (ShortUrl url : urls) {
                url.setEnabled(true); // 数据库状态恢复启用

                // 🌟 3. 同理，解封时也把老的缓存清一下
                redisTemplate.delete("shorturl:" + url.getShortCode());
            }
            shortUrlRepository.saveAll(urls);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "应用已解封，关联短链已恢复正常");
        return result;
    }

    // 5. 批量封禁应用
    @PatchMapping("/batch/disable")
    public Map<String, Object> batchDisableClients(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            disableClient(id);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "批量封禁成功，所有关联短链已失效");
        return result;
    }
}
