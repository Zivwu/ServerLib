package com.ziv.echosync.common.exception;

import com.ziv.echosync.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 * 在 Service 层主动抛出，由 GlobalExceptionHandler 统一捕获并返回对应的 Result
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ResultCode resultCode;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }
}
