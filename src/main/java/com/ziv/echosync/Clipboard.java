package com.ziv.echosync;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("clipboard") // 指定映射到数据库的哪张表
public class Clipboard {

    @TableId(type = IdType.AUTO) // 告诉框架这是主键，且自增
    private Long id;

    private String content;
    private String deviceName;
    private LocalDateTime createTime;

    // --- 下面是 Getters 和 Setters ---
    // (在 IDEA 里可以按快捷键 Cmd + N -> Getter and Setter 一键生成)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}