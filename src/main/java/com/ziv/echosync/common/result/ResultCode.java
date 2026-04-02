package com.ziv.echosync.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应状态码枚举
 * 定义所有接口可能返回的业务状态码和对应消息
 */
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
