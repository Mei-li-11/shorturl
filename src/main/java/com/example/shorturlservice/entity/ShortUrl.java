package com.example.shorturlservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity // 告诉 JPA：这不仅是一个 Java 类，还是数据库里的一张表
@Table(name = "short_urls") // 指定生成的表名叫 short_urls
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String name; // 名称

    @Column(nullable = false, length = 1000)
    private String originalUrl; // 原始长链接

    @Column(unique = true, nullable = false)
    private String shortCode; // 短码

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8") // 魔法：把时间格式化成漂亮的模样
    private LocalDateTime createdAt; // 创建时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expiresAt; // 过期时间

    @Column(nullable = false)
    @JsonProperty("status")
    private Boolean enabled = true; // 状态：是否启用

    private Long clickCount = 0L; // 点击次数

    private String appId; // 应用标识


    // ================= 以下是手动添加的 Getter 和 Setter =================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    @JsonProperty("status")
    public Boolean getEnabled() { return enabled; }

    @JsonProperty("status")
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Long getClickCount() { return clickCount; }
    public void setClickCount(Long clickCount) { this.clickCount = clickCount; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
}