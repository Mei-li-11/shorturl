package com.example.shorturlservice.config;

import com.example.shorturlservice.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 从请求头里找 "Authorization"
        String authHeader = request.getHeader("Authorization");

        // 2. 检查是不是以 "Bearer " 开头的标准 Token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // 把前缀 "Bearer " 砍掉，留下真正的 Token 字符串
            String token = authHeader.substring(7);

            // 3. 放到验钞机里验一下，取出用户名
            String username = jwtUtils.getUsernameFromToken(token);

            // 4. 如果用户名存在，且当前系统还没登记过这个人的身份
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 给他发一张“临时内部通行证”，证明他已经通过了咱们的校验
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());

                // 把通行证挂在这次请求的上下文中，后面的大门就全部对他敞开了
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 5. 检查完毕，放行！(让他进入下一个环节，如果没有通行证，后面会被 SecurityConfig 拦住)
        filterChain.doFilter(request, response);
    }
}