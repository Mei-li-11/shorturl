package com.example.shorturlservice.repository;

import com.example.shorturlservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 按用户名查找用户，登录时会用到
    User findByUsername(String username);
}