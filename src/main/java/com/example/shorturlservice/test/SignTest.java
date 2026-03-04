package com.example.shorturlservice.test;
import org.springframework.util.DigestUtils;

public class SignTest {
    public static void main(String[] args) {
        // 假设我是淘宝系统，这是你们分配给我的 AK 和 SK
        String appKey = "7b35fff745f345a0";
        String secretKey = "2419be3109bc48c8bb9d0245899525e4";

        // 获取当前精确到毫秒的时间戳
        String timestamp = String.valueOf(System.currentTimeMillis());

        // 🌟 核心算法：把 AK、时间戳、SK 拼在一起，进行 MD5 摘要加密
        String rawStr = appKey + timestamp + secretKey;
        String sign = DigestUtils.md5DigestAsHex(rawStr.getBytes());

        System.out.println("请把下面三个值填入 Postman 的 Headers 中：\n");
        System.out.println("X-App-Key: " + appKey);
        System.out.println("X-Timestamp: " + timestamp);
        System.out.println("X-Sign: " + sign);

        // 注意：生成后请在 5 分钟内发起请求，否则会触发咱们写的防重放拦截哦！
    }
}
