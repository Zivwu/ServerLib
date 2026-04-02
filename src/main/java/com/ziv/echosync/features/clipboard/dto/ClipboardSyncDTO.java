package com.ziv.echosync.features.clipboard.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 剪贴板同步请求 DTO
 */
@Data
public class ClipboardSyncDTO {

    @NotBlank(message = "内容不能为空")
    private String content;

    @NotBlank(message = "设备名不能为空")
    private String deviceName;
}
