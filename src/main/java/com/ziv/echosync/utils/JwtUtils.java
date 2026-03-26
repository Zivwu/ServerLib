package com.ziv.echosync.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtils {

    // ⚠️ 极其重要：这是你的服务器私钥（盐）。必须是足够长的随机字符串（至少 256 bits/32字符）。
    // 真实项目中这个绝不能写死在代码里，要写在配置文件中。
    private static final String SECRET_STRING = "ZivEchoSyncSuperSecretKey1234567890!!!";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

    // Token 有效期：这里设置为 7 天 (单位：毫秒)
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7;

    /**
     * 1. 根据设备名称生成 Token (签发通行证)
     */
    public static String generateToken(String deviceName) {
        return Jwts.builder()
                .subject(deviceName) // 存入主要信息（比如 userId 或设备名）
                .issuedAt(new Date()) // 签发时间
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 过期时间
                .signWith(KEY) // 使用服务器私钥签名防伪造
                .compact();
    }

    /**
     * 2. 解析和校验 Token (验票)
     * 如果 Token 被篡改或已过期，这里会直接抛出异常！
     */
    public static String parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(KEY) // 用同一把钥匙验证
                .build()
                .parseSignedClaims(token) // 如果解析失败、过期、被篡改，这里会抛出 JwtException
                .getPayload();

        return claims.getSubject(); // 返回我们当初存进去的设备名
    }
}