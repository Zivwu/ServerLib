# Step 02 — 创建 ResultCode 枚举

> 目标：定义统一的响应状态码，所有接口的成功/失败状态都从这里取值。

---

## 文件位置

`src/main/java/com/ziv/echosync/common/result/ResultCode.java`

---

## 完整代码

```java
package com.ziv.echosync.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;
}
```

---

## 逐行解释

**`@Getter`**
Lombok 注解，自动为 `code` 和 `message` 字段生成 `getCode()` 和 `getMessage()` 方法。
不用再手写：
```java
public int getCode() { return code; }
public String getMessage() { return message; }
```

**`@AllArgsConstructor`**
Lombok 注解，自动生成包含所有字段的构造方法。
枚举的每个常量（如 `SUCCESS(200, "操作成功")`）就是在调用这个构造方法。

**枚举常量对照表**

| 常量 | code | 使用场景 |
|------|------|---------|
| `SUCCESS` | 200 | 操作正常完成 |
| `BAD_REQUEST` | 400 | 客户端传参有问题（如字段为空） |
| `UNAUTHORIZED` | 401 | 没有登录或 Token 失效 |
| `FORBIDDEN` | 403 | 登录了但没有权限 |
| `NOT_FOUND` | 404 | 请求的资源不存在 |
| `INTERNAL_ERROR` | 500 | 服务器内部出错 |

---

## 使用方式

后续在代码中这样引用：

```java
// 抛出业务异常时
throw new BusinessException(ResultCode.UNAUTHORIZED);

// 构造错误响应时
return Result.error(ResultCode.BAD_REQUEST);
```
