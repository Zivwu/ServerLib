# Step 03 — 创建 Result\<T\> 统一响应体

> 目标：定义所有接口的统一返回格式，前端只需判断 `code` 是否为 200。

---

## 文件位置

`src/main/java/com/ziv/echosync/common/result/Result.java`

---

## 完整代码

```java
package com.ziv.echosync.common.result;

import lombok.Data;

@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = "操作成功";
        result.data = data;
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.code = resultCode.getCode();
        result.message = resultCode.getMessage();
        return result;
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }
}
```

---

## 解释

**`@Data`**
Lombok 的"全家桶"注解，等价于同时加上 `@Getter` + `@Setter` + `@ToString` + `@EqualsAndHashCode`。

**为什么用静态工厂方法？**

可读性更好，调用方不需要关心 code/message 的具体值：

```java
// 成功，带数据
return Result.success(clipboardVO);

// 成功，无数据
return Result.success();

// 失败，用枚举
return Result.error(ResultCode.UNAUTHORIZED);

// 失败，自定义消息
return Result.error(400, "设备名不能为空");
```

**前端收到的 JSON 格式**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "content": "Hello",
    "deviceName": "my-phone"
  }
}
```
