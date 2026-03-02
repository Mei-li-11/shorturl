package com.example.shorturlservice.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    // 自动生成一个超级安全的密钥（256位以上），专门用来给 Token 盖章签名
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 设置 Token 的有效期为 24 小时
    private final long EXPIRE_TIME = 1000 * 60 * 60 * 24;

    // 印发 Token 的魔法方法
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) // Token 里记录是谁登录的
                .setIssuedAt(new Date()) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME)) // 过期时间
                .signWith(key) // 盖上咱们专属的防伪印章
                .compact();
    }

    // 验钞机：检查 Token 是否有效，并从中取出用户名
    public String getUsernameFromToken(String token) {
        try {
            // 用我们独家的私钥 (key) 去解密这个 token
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject(); // 获取当时存进去的 username
        } catch (Exception e) {
            // 如果报错（比如 Token 过期了、被篡改了），直接返回 null
            return null;
        }
    }
}