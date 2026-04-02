# Step 10 — 创建 ClipboardService 和 ClipboardServiceImpl

> 目标：实现剪贴板同步和查询的核心业务逻辑，包含数据库写入、Redis 缓存、WebSocket 广播。

---

## 文件位置

- `src/main/java/com/ziv/echosync/features/clipboard/ClipboardService.java`
- `src/main/java/com/ziv/echosync/features/clipboard/ClipboardServiceImpl.java`

---

## ClipboardService 接口

```java
public interface ClipboardService {
    ClipboardVO syncClipboard(ClipboardSyncDTO dto);
    ClipboardVO getLatest();
}
```

---

## ClipboardServiceImpl 核心逻辑

### syncClipboard 流程

```
DTO → Entity → 写数据库 → 写 Redis → WebSocket 广播 → 返回 VO
```

```java
@Override
public ClipboardVO syncClipboard(ClipboardSyncDTO dto) {
    // 1. DTO → Entity
    Clipboard clipboard = new Clipboard();
    clipboard.setContent(dto.getContent());
    clipboard.setDeviceName(dto.getDeviceName());
    clipboard.setCreateTime(LocalDateTime.now());

    // 2. 持久化到数据库
    clipboardMapper.insert(clipboard);

    // 3. 更新 Redis 缓存（TTL 5 分钟）
    redisTemplate.opsForValue().set("clipboard:latest", clipboard, 5, TimeUnit.MINUTES);

    // 4. WebSocket 广播
    String json = objectMapper.writeValueAsString(clipboard);
    EchoWebSocketServer.broadcast(json);

    // 5. Entity → VO
    return toVO(clipboard);
}
```

### getLatest 流程（缓存优先）

```
读 Redis → 命中则返回 → 未命中则查数据库 → 回填 Redis → 返回 VO
```

```java
@Override
public ClipboardVO getLatest() {
    // 1. 优先读 Redis 缓存
    Object cached = redisTemplate.opsForValue().get("clipboard:latest");
    if (cached instanceof Clipboard clipboard) {
        return toVO(clipboard);
    }

    // 2. 缓存未命中，查数据库
    QueryWrapper<Clipboard> qw = new QueryWrapper<>();
    qw.orderByDesc("create_time").last("LIMIT 1");
    Clipboard clipboard = clipboardMapper.selectOne(qw);

    // 3. 回填缓存
    if (clipboard != null) {
        redisTemplate.opsForValue().set("clipboard:latest", clipboard, 5, TimeUnit.MINUTES);
    }

    return clipboard != null ? toVO(clipboard) : null;
}
```

---

## 降级策略

Redis 操作都包裹在 try-catch 中：
- Redis 写入失败 → 打印日志，继续执行（数据已写入数据库）
- Redis 读取失败 → 打印日志，降级查数据库

这样即使 Redis 服务不可用，核心功能依然正常。
