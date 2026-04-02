# Step 09 — 创建 Clipboard Entity / DTO / VO / Mapper

> 目标：为剪贴板模块建立数据层基础，包含数据库实体、请求入参、响应出参和数据访问接口。

---

## 文件位置

```
features/clipboard/
├── entity/
│   └── Clipboard.java        # 数据库实体
├── dto/
│   └── ClipboardSyncDTO.java # 同步请求入参
├── vo/
│   └── ClipboardVO.java      # 响应出参
└── ClipboardMapper.java      # 数据访问接口
```

---

## 9.1 Clipboard Entity

```java
package com.ziv.echosync.features.clipboard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("clipboard")
public class Clipboard {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String content;
    private String deviceName;
    private LocalDateTime createTime;
}
```

- `@Data`：Lombok 自动生成所有 getter/setter，替代原来手写的 20 行代码
- `@TableName("clipboard")`：指定映射到数据库的 `clipboard` 表
- `@TableId(type = IdType.AUTO)`：主键自增，插入后 MyBatis-Plus 会自动回填 id

---

## 9.2 ClipboardSyncDTO

```java
@Data
public class ClipboardSyncDTO {

    @NotBlank(message = "内容不能为空")
    private String content;

    @NotBlank(message = "设备名不能为空")
    private String deviceName;
}
```

---

## 9.3 ClipboardVO

```java
@Data
public class ClipboardVO {
    private Long id;
    private String content;
    private String deviceName;
    private LocalDateTime createTime;
}
```

---

## 9.4 ClipboardMapper

```java
@Mapper
public interface ClipboardMapper extends BaseMapper<Clipboard> {
}
```

继承 `BaseMapper<Clipboard>` 后，无需写任何 SQL，即可使用：
- `insert(entity)` — 插入
- `selectOne(wrapper)` — 查询单条
- `selectList(wrapper)` — 查询列表
- `updateById(entity)` — 按 id 更新
- `deleteById(id)` — 按 id 删除
