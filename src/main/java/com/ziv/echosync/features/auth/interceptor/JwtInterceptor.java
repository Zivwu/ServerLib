package com.ziv.echosync.features.auth.interceptor;

import com.ziv.echosync.common.exception.BusinessException;
import com.ziv.echosync.common.result.ResultCode;
import com.ziv.echosync.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证拦截器
 * 拦截所有非白名单请求，校验 Authorization 头中的 Bearer Token
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String authHeader = request.getHeader("Authorization");

        // 检查 Header 是否存在且格式正确
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 提取 Token（去掉 "Bearer " 前缀）
        String token = authHeader.substring(7);

        try {
            String deviceName = JwtUtils.parseToken(token);
            // 将设备名存入 request，后续 Controller 可以取用
            request.setAttribute("deviceName", deviceName);
            return true;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }
}
