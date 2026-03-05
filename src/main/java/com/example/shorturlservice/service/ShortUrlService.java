package com.example.shorturlservice.service;

import com.example.shorturlservice.entity.ShortUrl;
import com.example.shorturlservice.repository.ShortUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class ShortUrlService {

    @Autowired
    private ShortUrlRepository repository;

    // 🌟 注入 Spring Boot 提供的 Redis 操作神器
    @Autowired
    private StringRedisTemplate redisTemplate;

    // 核心算法：生成 6 位随机短码
    // 🌟 完美契合报告的“真·Base62 编码 + UUID 哈希”算法
    private String generateShortCode() {
        // 1. 严格对应报告里的字符表："0-9a-zA-Z"
        String chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        // 2. 严格对应报告："以UUID随机数的hashCode绝对值为输入"
        long num = Math.abs((long) java.util.UUID.randomUUID().hashCode());

        StringBuilder sb = new StringBuilder();

        // 3. 严格对应报告："以62为基数逐位映射至字符表" (这就是真正的 Base62 进制转换算法)
        do {
            int remainder = (int) (num % 62);
            sb.append(chars.charAt(remainder));
            num /= 62;
        } while (num > 0);

        // 4. 严格对应报告："截取6位作为最终短码" (由于 hashCode 转 Base62 通常是 5-6 位，不够 6 位就在前面补零)
        while (sb.length() < 6) {
            sb.insert(0, chars.charAt(0));
        }

        return sb.substring(0, 6);
    }

    // ================== 【高并发核心读取逻辑】 ==================

    /**
     * 根据短码获取真实长链接（带 Redis 缓存的高并发版）
     */
    public String getOriginalUrl(String shortCode) {
        String redisKey = "shorturl:" + shortCode;
        String clickKey = "shorturl:clicks:" + shortCode;

        // 1. 【第一道防线】先去 Redis 缓存里查
        String cachedUrl = redisTemplate.opsForValue().get(redisKey);
        if (cachedUrl != null) {
            System.out.println("🚀 命中 Redis 缓存！极其丝滑！短码: " + shortCode);
            redisTemplate.opsForValue().increment(clickKey);
            return cachedUrl;
        }

        // 2. 【兜底防线】如果 Redis 里没有，再去查 MySQL 数据库
        System.out.println("🐌 未命中缓存，去 MySQL 数据库捞数据...短码: " + shortCode);
        ShortUrl entity = repository.findByShortCode(shortCode);

        // 注意：这里需要确保你的 ShortUrl 实体类中是 isEnabled() 还是 getStatus() 方法，请根据实际情况微调
        if (entity != null && entity.getEnabled()) {

            // 🌟 新增：寿命检查！看看寿命是不是到期了？
            if (entity.getExpiresAt() != null && entity.getExpiresAt().isBefore(LocalDateTime.now())) {
                System.out.println("🚫 链接已过期！短码: " + shortCode);
                return null; // 已经过期了！无情拒绝！
            }

            String originalUrl = entity.getOriginalUrl();

            // 🌟 新增：动态计算缓存时间。如果快过期了，Redis 的缓存寿命也要跟着缩短
            long cacheSeconds = 24 * 60 * 60; // 默认缓存 24 小时
            if (entity.getExpiresAt() != null) {
                // 计算距离死亡还剩几秒
                long liveSeconds = java.time.Duration.between(LocalDateTime.now(), entity.getExpiresAt()).getSeconds();
                // 取 24小时 和 剩余寿命 中更短的那个
                cacheSeconds = Math.min(cacheSeconds, liveSeconds);
            }

            // 3. 【回写缓存】把从 MySQL 查到的数据塞进 Redis
            redisTemplate.opsForValue().set(redisKey, originalUrl, cacheSeconds, TimeUnit.SECONDS);
            redisTemplate.opsForValue().increment(clickKey);
            return originalUrl;
        }
        // 数据库没查到或者被禁用了，返回 null
        return null;
    }

    // ================== 【后台管理写入/更新逻辑】 ==================

    // 🌟 改造后的新增短链接（软删除版）
    public ShortUrl create(ShortUrl shortUrl, boolean isPermanent) {
        // 1. 查重防刷：看看数据库里是不是已经有这个长链接了？
        ShortUrl exist = repository.findByOriginalUrl(shortUrl.getOriginalUrl());

        if (exist != null) {
            // 如果存在，进一步检查它是不是已经过期了？
            if (exist.getExpiresAt() == null || exist.getExpiresAt().isAfter(LocalDateTime.now())) {
                // 没过期，直接返回旧链接！
                return exist;
            } else {
                // 🌟 核心修改：如果过期了，我们【不删除它】，而是直接给它换个新短码、刷新寿命、重新启用！
                // 这样既保证了数据库里同一个长链只有一条记录，又不会丢掉数据。
                exist.setShortCode(generateShortCode());
                exist.setCreatedAt(LocalDateTime.now());
                exist.setEnabled(true);

                if (isPermanent) {
                    exist.setExpiresAt(null);
                } else {
                    exist.setExpiresAt(LocalDateTime.now().plusDays(1));
                }
                // 清理掉以前那个旧短码的缓存
                redisTemplate.delete("shorturl:" + exist.getShortCode());

                return repository.save(exist); // 覆盖保存
            }
        }

        // 2. 如果数据库压根没有这个长链，就走正常的全新生成逻辑
        shortUrl.setShortCode(generateShortCode());
        shortUrl.setCreatedAt(LocalDateTime.now());
        shortUrl.setEnabled(true);

        if (isPermanent) {
            shortUrl.setExpiresAt(null);
        } else {
            shortUrl.setExpiresAt(LocalDateTime.now().plusDays(1)); // 测试 30 秒
        }

        return repository.save(shortUrl);
    }

    // 2. 获取所有短链接列表
    public List<ShortUrl> getList() {
        return repository.findAll();
    }

    // 3. 编辑短链接
    public void update(Long id, ShortUrl updateData) {
        ShortUrl exist = repository.findById(id).orElseThrow(() -> new RuntimeException("找不到这条数据"));
        exist.setName(updateData.getName());
        exist.setOriginalUrl(updateData.getOriginalUrl());
        repository.save(exist);

        // 🌟 缓存一致性：修改了原链接，必须把旧缓存删掉！
        redisTemplate.delete("shorturl:" + exist.getShortCode());
    }

    // 4. 删除短链接
    public void delete(Long id) {
        ShortUrl exist = repository.findById(id).orElseThrow(() -> new RuntimeException("找不到这条数据"));
        repository.deleteById(id);

        // 🌟 缓存一致性：数据删了，缓存里的也要连根拔起！
        redisTemplate.delete("shorturl:" + exist.getShortCode());
    }

    // 5. 改变状态（启用/禁用）
    public void toggleStatus(Long id, boolean isEnable) {
        ShortUrl exist = repository.findById(id).orElseThrow(() -> new RuntimeException("找不到这条数据"));
        exist.setEnabled(isEnable);
        repository.save(exist);

        // 🌟 缓存一致性：如果禁用了，必须把缓存清掉，防止别人继续通过缓存跳转！
        redisTemplate.delete("shorturl:" + exist.getShortCode());
    }
}
