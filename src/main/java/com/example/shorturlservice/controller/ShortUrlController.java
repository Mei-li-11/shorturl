package com.example.shorturlservice.controller;

import com.example.shorturlservice.entity.ShortUrl;
import com.example.shorturlservice.service.ShortUrlService;
import com.example.shorturlservice.repository.ShortUrlRepository; // 🌟 1. 新增：引入仓库用来做批量操作
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.shorturlservice.annotation.RateLimit;

import java.time.LocalDateTime; // 🌟 2. 新增：引入时间类用来判断是否过期
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin // 魔法注解：允许跨域请求！没有它，Vue 连不上咱们的 Spring Boot
@RestController
@RequestMapping("/api/shortlinks") // 统一的接口前缀，和前端 axios 里的路径完全对应
public class ShortUrlController {

    @Autowired
    private ShortUrlService service;

    // 🌟 3. 新增：注入 Repository（有了它，批量删除极其方便）
    @Autowired
    private ShortUrlRepository shortUrlRepository;

    // 统一的返回格式封装，匹配前端的 { code: 200, data: ..., message: ... }
    private Map<String, Object> success(Object data, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", data);
        result.put("message", message);
        return result;
    }

    // 1. 获取列表（支持按名称搜索）
    @GetMapping
    public Map<String, Object> getList(@RequestParam(required = false) String name) {
        List<ShortUrl> list = service.getList();
        // 如果前端传了搜索名字，我们就过滤一下再返回
        if (name != null && !name.trim().isEmpty()) {
            list = list.stream()
                    .filter(item -> item.getName() != null && item.getName().contains(name))
                    .collect(Collectors.toList());
        }
        return success(list, "查询成功");
    }

    // 2. 新增
    // 1. 后台管理新增接口 (传 true，永久有效)
    @PostMapping
    public Map<String, Object> add(@RequestBody ShortUrl shortUrl) {
        ShortUrl savedUrl = service.create(shortUrl, true);
        return success(savedUrl, "新增成功");
    }

    // 2. 前台演示生成专用接口 (传 false，只活 1 天，带防刷拦截)
    @RateLimit(time = 60, count = 3)
    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestBody ShortUrl shortUrl) {
        ShortUrl savedUrl = service.create(shortUrl, false);
        return success(savedUrl, "生成成功，短链接有效期为 24 小时");
    }

    // 3. 编辑
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody ShortUrl shortUrl) {
        service.update(id, shortUrl);
        return success(null, "编辑成功");
    }

    // 4. 删除
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        service.delete(id);
        return success(null, "删除成功");
    }

    // 5. 启用
    @PatchMapping("/{id}/enable")
    public Map<String, Object> enable(@PathVariable Long id) {
        service.toggleStatus(id, true);
        return success(null, "已启用");
    }

    // 6. 禁用
    @PatchMapping("/{id}/disable")
    public Map<String, Object> disable(@PathVariable Long id) {
        service.toggleStatus(id, false);
        return success(null, "已禁用");
    }

    // ==========================================
    // 👇 🌟 4. 新增的两个超强清理接口
    // ==========================================

    // 7. 批量删除短链
    @DeleteMapping("/batch")
    public Map<String, Object> batchDelete(@RequestBody List<Long> ids) {
        // JPA 的神级方法，传一个 ID 列表进去，直接全删
        shortUrlRepository.deleteAllById(ids);
        return success(null, "批量删除成功");
    }

    // 8. 一键清理所有已过期的短链
    @DeleteMapping("/expired")
    public Map<String, Object> cleanExpiredUrls() {
        List<ShortUrl> allUrls = shortUrlRepository.findAll();
        int count = 0;
        LocalDateTime now = LocalDateTime.now();

        for (ShortUrl url : allUrls) {
            // 判断逻辑：如果设置了过期时间 (不为 null) 且时间早于现在 (isBefore)
            if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(now)) {
                shortUrlRepository.delete(url);
                count++;
            }
        }

        return success(null, "成功清理了 " + count + " 个过期垃圾短链");
    }
}