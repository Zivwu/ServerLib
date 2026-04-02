# Step 05 — 创建 Auth 模块 DTO 和 VO

> 目标：定义认证模块的入参（DTO）和出参（VO），实现 Controller 与 Entity 的解耦。

---

## 5.1 AuthLoginDTO（登录请求入参）

**文件位置：** `src/main/java/com/ziv/echosync/features/auth/dto/AuthLoginDTO.java`

```java
package com.ziv.echosync.features.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthLoginDTO {

    @NotBlank(message = "设备名不能为空")
    private String deviceName;
}
```

**说明：**
- `@Data`：Lombok 自动生成 getter/setter
- `@NotBlank`：当 deviceName 为 null、空字符串或纯空格时，Spring 会抛出 `MethodArgumentNotValidException`，由 `GlobalExceptionHandler` 捕获并返回 code=400

---

## 5.2 AuthTokenVO（登录响应出参）

**文件位置：** `src/main/java/com/ziv/echosync/features/auth/vo/AuthTokenVO.java`

```java
package com.ziv.echosync.features.auth.vo;

import lombok.Data;

@Data
public class AuthTokenVO {

    private String token;
    private String deviceName;
}
```

**说明：**
- VO 只包含需要返回给客户端的字段，不暴露内部实现细节
- 客户端拿到 token 后，后续请求都需要在 Header 中携带：`Authorization: Bearer <token>`

---

## DTO vs VO vs Entity 的区别

| 类型 | 方向 | 用途 |
|------|------|------|
| DTO | 客户端 → 服务端 | 接收请求参数，可以加校验注解 |
| VO | 服务端 → 客户端 | 返回响应数据，只包含需要暴露的字段 |
| Entity | 服务端 ↔ 数据库 | 与数据库表映射，不直接暴露给外部 |
