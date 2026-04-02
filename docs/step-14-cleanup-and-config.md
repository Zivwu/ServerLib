# Step 14 — 清理旧代码 + MyBatisPlusConfig

> 目标：删除原来扁平结构的旧文件，添加 MyBatisPlusConfig 确保新包路径下的 Mapper 能被扫描到。

---

## 14.1 删除旧文件

以下文件已被删除（功能已迁移到 features/ 对应模块）：

| 删除的文件 | 迁移到 |
|-----------|--------|
| `AuthController.java` | `features/auth/AuthController.java` |
| `Clipboard.java` | `features/clipboard/entity/Clipboard.java` |
| `ClipboardController.java` | `features/clipboard/ClipboardController.java` |
| `ClipboardMapper.java` | `features/clipboard/ClipboardMapper.java` |
| `HelloController.java` | 已废弃，不再需要 |
| `service/ClipboardService.java` | `features/clipboard/ClipboardService.java` |
| `service/impl/ClipboardServiceImpl.java` | `features/clipboard/ClipboardServiceImpl.java` |
| `config/WebSocketConfig.java` | `features/websocket/WebSocketConfig.java` |
| `websocket/EchoWebSocketServer.java` | `features/websocket/EchoWebSocketServer.java` |

---

## 14.2 MyBatisPlusConfig

```java
@Configuration
@MapperScan("com.ziv.echosync.features")
public class MyBatisPlusConfig {
}
```

**为什么需要这个？**

原来的 `ClipboardMapper` 在根包下，`@SpringBootApplication` 的默认扫描范围能覆盖到它。

迁移到 `features/clipboard/` 后，虽然还在 `com.ziv.echosync` 包下，但为了明确声明扫描范围，加上 `@MapperScan` 更规范，也方便后续添加新的 feature 模块。

---

## 最终包结构

```
com.ziv.echosync/
├── EchoSyncApplication.java
├── common/
│   ├── config/
│   │   ├── MyBatisPlusConfig.java
│   │   ├── RedisConfig.java
│   │   └── WebMvcConfig.java
│   ├── exception/
│   │   ├── BusinessException.java
│   │   └── GlobalExceptionHandler.java
│   └── result/
│       ├── Result.java
│       └── ResultCode.java
├── features/
│   ├── auth/
│   │   ├── AuthController.java
│   │   ├── AuthService.java
│   │   ├── AuthServiceImpl.java
│   │   ├── dto/AuthLoginDTO.java
│   │   ├── interceptor/JwtInterceptor.java
│   │   └── vo/AuthTokenVO.java
│   ├── clipboard/
│   │   ├── ClipboardController.java
│   │   ├── ClipboardMapper.java
│   │   ├── ClipboardService.java
│   │   ├── ClipboardServiceImpl.java
│   │   ├── dto/ClipboardSyncDTO.java
│   │   ├── entity/Clipboard.java
│   │   └── vo/ClipboardVO.java
│   └── websocket/
│       ├── EchoWebSocketServer.java
│       └── WebSocketConfig.java
└── utils/
    └── JwtUtils.java
```
