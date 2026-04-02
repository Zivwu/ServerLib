package com.ziv.echosync.common.exception;

import com.ziv.echosync.common.result.Result;
import com.ziv.echosync.common.result.ResultCode;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一捕获所有 Controller 抛出的异常，转换为标准 Result 格式返回
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理业务异常（主动抛出的 BusinessException）
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getResultCode());
    }

    // 处理 @Valid 校验失败（DTO 字段不满足约束时 Spring 自动抛出）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = fieldError != null ? fieldError.getDefaultMessage() : ResultCode.BAD_REQUEST.getMessage();
        return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
    }

    // 兜底：处理所有未预期的异常
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.error(ResultCode.INTERNAL_ERROR);
    }
}
