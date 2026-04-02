# Step 07 — 创建 AuthController

> 目标：提供 POST /api/auth/login 接口，接收 JSON 请求体，返回统一 Result 格式的 Token。

---

## 文件位置

`src/main/java/com/ziv/echosync/features/auth/AuthController.java`

---

## 完整代码

```java
package com.ziv.echosync.features.auth;

import com.ziv.echosync.common.result.Result;
import com.ziv.echosync.features.auth.dto.AuthLoginDTO;
import com.ziv.echosync.features.auth.vo.AuthTokenVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<AuthTokenVO> login(@Valid @RequestBody AuthLoginDTO dto) {
        return Result.success(authService.login(dto));
    }
}
```

---

## 关键点

**`@Valid`**
触发 `AuthLoginDTO` 上的校验注解（`@NotBlank`）。如果校验失败，Spring 抛出 `MethodArgumentNotValidException`，由 `GlobalExceptionHandler` 捕获返回 400。

**`@RequestBody`**
从请求体中读取 JSON 并反序列化为 `AuthLoginDTO`。这替代了原来的 `@RequestParam`，支持 JSON 格式请求。

**返回类型 `Result<AuthTokenVO>`**
Controller 不再直接返回字符串，而是返回统一的 `Result` 包装。

---

## 测试

```bash
# 正常登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"deviceName": "my-phone"}'

# 预期响应
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "deviceName": "my-phone"
  }
}

# 空设备名（触发校验）
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"deviceName": ""}'

# 预期响应
{
  "code": 400,
  "message": "设备名不能为空",
  "data": null
}
```
