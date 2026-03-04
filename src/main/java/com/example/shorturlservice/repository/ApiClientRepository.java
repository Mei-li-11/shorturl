package com.example.shorturlservice.repository;

import com.example.shorturlservice.entity.ApiClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiClientRepository extends JpaRepository<ApiClient, Long> {

    // 🌟 魔法方法：根据 AK 寻找对应的凭证记录
    ApiClient findByAccessKey(String accessKey);
}