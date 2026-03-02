package com.example.shorturlservice.controller;

import com.example.shorturlservice.dto.LoginRequest;
import com.example.shorturlservice.entity.User;
import com.example.shorturlservice.repository.UserRepository;
import com.example.shorturlservice.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    // 密码比对器
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();

        // 1. 去数据库里找这个用户
        User user = userRepository.findByUsername(request.getUsername());

        // 2. 核心大招：调用 matches 方法，让 Spring 自动比对 前端传来的明文 和 数据库里的密文！
        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // 3. 密码正确！立刻用印钞机给他颁发一个专属 Token
            String token = jwtUtils.generateToken(user.getUsername());

            response.put("code", 200);
            response.put("message", "登录成功");
            response.put("token", token); // 把这把通往后台的“钥匙”扔给前端
        } else {
            response.put("code", 401);
            response.put("message", "用户名或密码错误，你是黑客吗？");
        }
        return response;
    }
}