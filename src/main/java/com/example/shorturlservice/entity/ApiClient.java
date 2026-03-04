package com.example.shorturlservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_clients") // 存外部应用凭证的表
public class ApiClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 外部应用的名字（比如：淘宝营销系统、京东短信系统）
    private String appName;

    // Access Key (公钥/应用ID) - 必须唯一
    @Column(unique = true, nullable = false)
    private String accessKey;

    // Secret Key (私钥/秘钥)
    @Column(nullable = false)
    private String secretKey;

    // 是否启用（如果我们发现某个应用乱发违规链接，可以在后台把这里设为 false 停用它）
    private Boolean enabled = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    // ================= Getters and Setters =================
    // 请在这里使用 IDEA 自动生成或者手写所有的 get 和 set 方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }
    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}