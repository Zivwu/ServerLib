package com.ziv.echosync.features.auth.vo;

import lombok.Data;

/**
 * 设备登录响应 VO
 */
@Data
public class AuthTokenVO {

    private String token;
    private String deviceName;
}
