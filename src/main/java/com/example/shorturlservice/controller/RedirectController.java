package com.example.shorturlservice.controller;

import com.example.shorturlservice.service.ShortUrlService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

@Controller
public class RedirectController {

    // 🌟 1. 把以前直接查数据库的 Repository，换成咱们刚升级过带 Redis 缓存的 Service
    @Autowired
    private ShortUrlService shortUrlService;

    @GetMapping("/{shortCode}")
    public void redirect(@PathVariable String shortCode, HttpServletResponse response) throws IOException {

        // 🌟 2. 见证奇迹：这行代码会优先去 Redis 里找长链接，速度快如闪电！
        String originalUrl = shortUrlService.getOriginalUrl(shortCode);

        if (originalUrl != null) {
            // 💡 提示：为了扛住极高并发，我们暂时把“同步写 MySQL 增加点击量”的代码拿掉了。
            // 真实的大厂做法是：这里直接跳转，把增加点击量的任务扔给后台异步线程去慢慢写。

            // 3. 瞬间起飞，重定向到目标网站
            response.sendRedirect(originalUrl);
        } else {
            // 4. 如果 Redis 和 MySQL 里都没找到（或者被禁用了），返回你之前写的友好提示
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("<h1 style='text-align:center;margin-top:50px;'>🚫 哎呀，这个短链接无效或已被禁用！</h1>");
        }
    }
}