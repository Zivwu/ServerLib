# Step 06 — 创建 AuthService 接口和实现

> 目标：将登录业务逻辑从 Controller 中抽离到 Service 层，Controller 只负责接收请求和返回响应。

---

## 文件位置

- `src/main/java/com/ziv/echosync/features/auth/AuthService.java`
- `src/main/java/com/ziv/echosync/features/auth/AuthServiceImpl.java`

---

## AuthService 接口

```java
package com.ziv.echosync.features.auth;

import com.ziv.echosync.features.auth.dto.AuthLoginDTO;
import com.ziv.echosync.features.auth.vo.AuthTokenVO;

public interface AuthService {
    AuthTokenVO login(AuthLoginDTO dto);
}
```

**为什么要定义接口？**
面向接口编程是 Spring 的核心思想。Controller 注入的是接口类型，不依赖具体实现，方便后续替换实现（比如加密码校验）而不影响 Controller 代码。

---

## AuthServiceImpl 实现

```java
package com.ziv.echosync.features.auth;

import com.ziv.echosync.features.auth.dto.AuthLoginDTO;
import com.ziv.echosync.features.auth.vo.AuthTokenVO;
import com.ziv.echosync.utils.JwtUtils;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public AuthTokenVO login(AuthLoginDTO dto) {
        String token = JwtUtils.generateToken(dto.getDeviceName());
        AuthTokenVO vo = new AuthTokenVO();
        vo.setToken(token);
        vo.setDeviceName(dto.getDeviceName());
        return vo;
    }
}
```

**`@Service`**
告诉 Spring 这是一个业务逻辑层的 Bean，启动时自动注册到容器，Controller 可以通过 `@Autowired` 注入。

**流程：**
1. 接收 `AuthLoginDTO`（包含 deviceName）
2. 调用 `JwtUtils.generateToken(deviceName)` 生成 JWT Token
3. 组装 `AuthTokenVO` 返回
