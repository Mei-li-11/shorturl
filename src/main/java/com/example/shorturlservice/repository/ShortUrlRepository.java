package com.example.shorturlservice.repository;

import com.example.shorturlservice.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// 🌟 新增导入：我们需要用到 List 集合和 LocalDateTime 时间类
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    // 魔法方法：只需要按照规范写对方法名，Spring 会自动帮你生成查询的 SQL！
    ShortUrl findByShortCode(String shortCode);

    // 查重用的魔法方法
    ShortUrl findByOriginalUrl(String originalUrl);

    // 👇 🌟 核心新增：专门给定时任务（巡逻兵）用的查询魔法！
    // Spring 会自动把它翻译成 SQL：SELECT * FROM short_urls WHERE expires_at < 传入的时间 AND enabled = true
    List<ShortUrl> findByExpiresAtBeforeAndEnabledTrue(LocalDateTime now);
}