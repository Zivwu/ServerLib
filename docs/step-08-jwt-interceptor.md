# Step 08 — 创建 JwtInterceptor 和 WebMvcConfig

> 目标：让所有受保护的接口都需要携带有效的 JWT Token，否则返回 401。

---

## 文件位置

- `src/main/java/com/ziv/echosync/features/auth/interceptor/JwtInterceptor.java`
- `src/main/java/com/ziv/echosync/common/config/WebMvcConfig.java`

---

## 8.1 JwtInterceptor

```java
package com.ziv.echosync.features.auth.interceptor;

import com.ziv.echosync.common.exception.BusinessException;
import com.ziv.echosync.common.result.ResultCode;
import com.ziv.echosync.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        try {
            String deviceName = JwtUtils.parseToken(token);
            request.setAttribute("deviceName", deviceName);
            return true;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }
}
```

**流程：**
1. 从请求头取 `Authorization` 字段
2. 检查是否以 `Bearer ` 开头（注意有空格）
3. 截取 Token 字符串（去掉前 7 个字符 `"Bearer "`）
4. 调用 `JwtUtils.parseToken()` 校验，失败则抛 `BusinessException(UNAUTHORIZED)`
5. 校验成功，把 deviceName 存入 request attribute，返回 `true` 放行

---

## 8.2 WebMvcConfig

```java
package com.ziv.echosync.common.config;

import com.ziv.echosync.features.auth.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }
}
```

**`addPathPatterns("/api/**")`** — 拦截所有 `/api/` 开头的路径
**`excludePathPatterns("/api/auth/login")`** — 登录接口加入白名单，不需要 Token

---

## 测试

```bash
# 不带 Token 访问受保护接口
curl http://localhost:8080/api/clipboard/latest

# 预期响应
{
  "code": 401,
  "message": "未授权",
  "data": null
}

# 带有效 Token 访问
curl http://localhost:8080/api/clipboard/latest \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# 预期响应（有数据时）
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```
