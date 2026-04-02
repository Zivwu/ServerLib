package com.ziv.echosync.features.clipboard.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 剪贴板响应 VO
 */
@Data
public class ClipboardVO {

    private Long id;
    private String content;
    private String deviceName;
    private LocalDateTime createTime;
}
