# Step 04 — 创建 BusinessException 和 GlobalExceptionHandler

> 目标：统一处理所有异常，让任何错误都以标准 Result 格式返回，Controller 里不再需要 try-catch。

---

## 文件位置

- `src/main/java/com/ziv/echosync/common/exception/BusinessException.java`
- `src/main/java/com/ziv/echosync/common/exception/GlobalExceptionHandler.java`

---

## 4.1 BusinessException

```java
package com.ziv.echosync.common.exception;

import com.ziv.echosync.common.result.ResultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ResultCode resultCode;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }
}
```

**为什么继承 RuntimeException？**

- Spring `@Transactional` 默认只回滚 `RuntimeException`，继承它可以确保事务正确回滚
- 调用方不需要强制 `try-catch`，由全局处理器统一兜底
- 代码更简洁，Service 方法签名不会被 `throws` 污染

**使用方式：**

```java
// 在 JwtInterceptor 或 Service 中这样抛出
throw new BusinessException(ResultCode.UNAUTHORIZED);
```

---

## 4.2 GlobalExceptionHandler

```java
package com.ziv.echosync.common.exception;

import com.ziv.echosync.common.result.Result;
import com.ziv.echosync.common.result.ResultCode;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getResultCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = fieldError != null ? fieldError.getDefaultMessage() : ResultCode.BAD_REQUEST.getMessage();
        return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.error(ResultCode.INTERNAL_ERROR);
    }
}
```

**`@RestControllerAdvice` = `@ControllerAdvice` + `@ResponseBody`**
拦截所有 Controller 抛出的异常，把处理结果以 JSON 格式返回。

**三个处理方法的分工：**

| 方法 | 捕获的异常 | 返回 |
|------|-----------|------|
| `handleBusinessException` | 主动抛出的业务异常 | 对应 ResultCode 的 code 和 message |
| `handleValidationException` | `@Valid` 校验失败 | code=400 + 具体字段校验失败信息 |
| `handleException` | 所有其他未预期异常 | code=500 |

---

## 验证

启动后发送一个缺少必填字段的请求：

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"deviceName": ""}'
```

预期响应：
```json
{
  "code": 400,
  "message": "设备名不能为空",
  "data": null
}
```
