package com.example.shorturlservice.task;

import com.example.shorturlservice.entity.ShortUrl;
import com.example.shorturlservice.repository.ShortUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ClickCountSyncTask {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ShortUrlRepository repository;

    // 🌟 核心：Cron 表达式，代表“每隔 1 分钟执行一次”
    // 面试官最爱问的定时任务原理就在这里！
    @Scheduled(cron = "0 */1 * * * ?")
    public void syncClickCount() {
        // 1. 找到 Redis 里所有记录点击量的账本 (模糊匹配所有以 shorturl:clicks: 开头的 key)
        Set<String> keys = redisTemplate.keys("shorturl:clicks:*");
        if (keys == null || keys.isEmpty()) {
            return; // 没人点击，清洁工继续休息
        }

        System.out.println("🔄 定时任务启动：准备同步 " + keys.size() + " 个链接的点击量到 MySQL...");

        for (String key : keys) {
            // 从 key 里提取出真实的短码 (例如 "shorturl:clicks:abcd12" 提取出 "abcd12")
            String shortCode = key.substring(16);

            // 拿到这一分钟内积攒的点击量
            String countStr = redisTemplate.opsForValue().get(key);
            if (countStr != null) {
                long newClicks = Long.parseLong(countStr);

                // 去数据库里找到对应的记录
                ShortUrl shortUrl = repository.findByShortCode(shortCode);
                if (shortUrl != null) {
                    // 把原来的点击量 加上 新增加的点击量
                    // ⚠️ 注意：确保你的 ShortUrl 实体类里有 getClickCount 和 setClickCount 方法！
                    // 如果实体类里没给默认值，getClickCount() 可能会是 null，所以需要判空兜底：
                    Long oldClicks = shortUrl.getClickCount() == null ? 0L : shortUrl.getClickCount();
                    shortUrl.setClickCount(oldClicks + newClicks);

                    repository.save(shortUrl);
                }

                // 同步完之后，把 Redis 里的账本撕掉（清零），等下一分钟重新记账！
                redisTemplate.delete(key);
            }
        }
        System.out.println("✅ 同步完成！MySQL 数据已是最新的！");
    }
}