package com.example.shorturlservice.task;

import com.example.shorturlservice.entity.ShortUrl;
import com.example.shorturlservice.repository.ShortUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ExpiredUrlCleanTask {

    @Autowired
    private ShortUrlRepository repository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 🌟 Cron 表达式：目前设定为“每 1 分钟执行一次”方便咱们测试。
    // 在真正的企业生产环境中，一般改成 "0 0 3 * * ?" 代表每天凌晨 3 点执行
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanExpiredUrls() {
        // 1. 去数据库把“寿命已尽”但“依然显示启用”的链接抓出来
        List<ShortUrl> expiredUrls = repository.findByExpiresAtBeforeAndEnabledTrue(LocalDateTime.now());

        if (expiredUrls.isEmpty()) {
            return; // 没有过期的，继续睡大觉
        }

        System.out.println("🧹 触发定期清理：发现 " + expiredUrls.size() + " 个已过期的短链，准备统一禁用！");

        // 2. 挨个处理：给它们挂上“禁用”牌子，并销毁缓存
        for (ShortUrl url : expiredUrls) {
            // 🌟 核心逻辑：绝对不删数据！只是把状态改成 false，这样后台依然能看到，且显示“已禁用”
            url.setEnabled(false);

            // 顺手把 Redis 里的缓存清理掉，确保没人能通过缓存继续访问
            redisTemplate.delete("shorturl:" + url.getShortCode());
        }

        // 3. 批量保存回数据库
        repository.saveAll(expiredUrls);

        System.out.println("✅ 定期清理完成！过期链接已全部变为禁用状态留存后台！");
    }
}