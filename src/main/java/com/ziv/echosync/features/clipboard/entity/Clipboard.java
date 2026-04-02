package com.ziv.echosync.features.clipboard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 剪贴板数据库实体
 */
@Data
@TableName("clipboard")
public class Clipboard {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String content;

    private String deviceName;

    private LocalDateTime createTime;
}
