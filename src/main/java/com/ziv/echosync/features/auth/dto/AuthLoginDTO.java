package com.ziv.echosync.features.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 设备登录请求 DTO
 */
@Data
public class AuthLoginDTO {

    @NotBlank(message = "设备名不能为空")
    private String deviceName;
}
