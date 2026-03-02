package com.example.shorturlservice.config;

import com.example.shorturlservice.entity.User;
import com.example.shorturlservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // 创建一个密码加密器
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // 检查数据库里有没有 admin 账号，没有的话就自动创建一个
        if (userRepository.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            // ⚠️ 划重点：这里我们把明文 "123456" 变成了极其复杂的 BCrypt 密文！
            admin.setPassword(passwordEncoder.encode("123456"));
            userRepository.save(admin);

            System.out.println("==========================================");
            System.out.println("🎉 默认管理员账号已生成！");
            System.out.println("👉 账号: admin");
            System.out.println("👉 密码: 123456 (数据库中已加密存储)");
            System.out.println("==========================================");
        }
    }
}