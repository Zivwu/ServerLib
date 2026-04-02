# 实现计划：EchoSync 企业级架构重构

## 概述

将 EchoSync 项目从扁平结构重构为标准企业级分层架构，按教程模式逐步实现。每个大步骤对应一个功能模块，配套教程文档输出到 `docs/` 目录。使用 Java（Spring Boot 3.5）+ Lombok + MyBatis-Plus + Redis + WebSocket + JWT。

## 任务列表

- [x] 1. Maven 依赖配置 + Lombok 集成
  - [x] 1.1 更新 pom.xml 添加新依赖
    - 添加 `lombok` 依赖（provided scope）并配置 maven-compiler-plugin 的 annotationProcessorPaths
    - 添加 `spring-boot-starter-data-redis` 依赖
    - 添加 `spring-boot-starter-validation` 依赖
    - 保留现有依赖不变
    - _需求: 11.1, 11.2, 11.3, 11.4, 10.1_

- [x] 2. common 基础设施模块
  - [x] 2.1 创建 ResultCode 枚举
    - 创建 `com.ziv.echosync.common.result.ResultCode` 枚举
    - 定义 SUCCESS(200)、BAD_REQUEST(400)、UNAUTHORIZED(401)、FORBIDDEN(403)、NOT_FOUND(404)、INTERNAL_ERROR(500)
    - 使用 Lombok @Getter 和 @AllArgsConstructor
    - _需求: 1.4, 1.6_

  - [x] 2.2 创建 Result\<T\> 统一响应体
    - 创建 `com.ziv.echosync.common.result.Result<T>` 类
    - 包含 code、message、data 三个字段，使用 @Data 注解
    - 实现 success(T data)、success()、error(ResultCode)、error(int code, String message) 静态工厂方法
    - _需求: 1.1, 1.2, 1.3, 1.5_

  - [x] 2.3 创建 BusinessException 自定义异常
    - 创建 `com.ziv.echosync.common.exception.BusinessException`
    - 携带 ResultCode 字段，使用 Lombok @Getter
    - 提供接收 ResultCode 参数的构造方法
    - _需求: 2.4_

  - [x] 2.4 创建 GlobalExceptionHandler 全局异常处理器
    - 创建 `com.ziv.echosync.common.exception.GlobalExceptionHandler`
    - 使用 @RestControllerAdvice 注解
    - 处理 BusinessException → 返回对应 ResultCode 的 Result
    - 处理 MethodArgumentNotValidException → 返回 code=400 + 具体校验失败信息
    - 处理 Exception → 返回 code=500
    - _需求: 2.1, 2.2, 2.3, 2.5_


  - [ ]* 2.5 编写 Result 和 GlobalExceptionHandler 的单元测试
    - 测试 Result.success() 返回 code=200
    - 测试 Result.error(ResultCode) 返回对应 code 和 message
    - 测试 GlobalExceptionHandler 处理 BusinessException 的映射正确性
    - **Property 1: Result 工厂方法正确性**
    - **Property 2: 异常处理映射正确性**
    - **验证: 需求 1.1, 1.2, 1.3, 2.1, 2.3**

  - [x] 2.6 编写 docs/01-common-基础设施搭建.md 教程文档
    - 涵盖 Lombok 配置说明、Result、ResultCode、BusinessException、GlobalExceptionHandler
    - 按小步骤拆分，每步包含代码示例和解释说明
    - _需求: 12.1, 12.6_

- [x] 3. 检查点 - 确认基础设施模块
  - 确保代码编译通过，所有测试通过，如有问题请向用户确认。

- [x] 4. auth 认证模块
  - [x] 4.1 创建 AuthLoginDTO
    - 创建 `com.ziv.echosync.features.auth.dto.AuthLoginDTO`
    - 包含 deviceName 字段，使用 @Data 和 @NotBlank 校验注解
    - _需求: 3.5, 3.6, 6.3_

  - [x] 4.2 创建 AuthTokenVO
    - 创建 `com.ziv.echosync.features.auth.vo.AuthTokenVO`
    - 包含 token 和 deviceName 字段，使用 @Data
    - _需求: 3.6, 6.2_

  - [x] 4.3 创建 AuthService 接口和 AuthServiceImpl 实现
    - 创建 `com.ziv.echosync.features.auth.AuthService` 接口
    - 创建 `com.ziv.echosync.features.auth.AuthServiceImpl` 实现类
    - 实现 login(AuthLoginDTO) 方法：调用 JwtUtils.generateToken，返回 AuthTokenVO
    - _需求: 6.1, 6.2_

  - [x] 4.4 创建新的 AuthController
    - 创建 `com.ziv.echosync.features.auth.AuthController`
    - POST /api/auth/login 接口，使用 @RequestBody + @Valid 接收 AuthLoginDTO
    - 返回 Result\<AuthTokenVO\>
    - _需求: 6.1, 6.2, 6.3, 3.1, 3.2_

  - [x] 4.5 创建 JwtInterceptor
    - 创建 `com.ziv.echosync.features.auth.interceptor.JwtInterceptor`
    - 实现 HandlerInterceptor.preHandle 方法
    - 从 Authorization 头提取 Bearer Token，调用 JwtUtils.parseToken 校验
    - 校验失败抛出 BusinessException(UNAUTHORIZED)
    - 校验成功将 deviceName 存入 request attribute
    - _需求: 4.1, 4.2, 4.3, 4.5_

  - [x] 4.6 创建 WebMvcConfig 注册拦截器
    - 创建 `com.ziv.echosync.common.config.WebMvcConfig`
    - 注册 JwtInterceptor，拦截 /api/** 路径
    - 将 /api/auth/login 加入白名单（excludePathPatterns）
    - _需求: 4.4_

  - [ ]* 4.7 编写 auth 模块单元测试
    - 测试 AuthService.login 返回有效 Token
    - 测试 JwtInterceptor 对无效 Token 的拒绝
    - **Property 5: JWT 拦截器对无效请求的拒绝**
    - **Property 6: JWT Token 生成与解析 round-trip**
    - **Property 8: 登录流程返回有效 Token**
    - **验证: 需求 4.1, 4.2, 4.3, 4.5, 6.1, 6.2**

  - [x] 4.8 编写 docs/02-auth-认证模块.md 教程文档
    - 涵盖 AuthLoginDTO、AuthTokenVO、AuthService、AuthController、JwtInterceptor、WebMvcConfig
    - 按小步骤拆分，每步包含代码示例和解释说明
    - _需求: 12.2, 12.6_

- [x] 5. 检查点 - 确认认证模块
  - 确保代码编译通过，所有测试通过，如有问题请向用户确认。

- [ ] 6. clipboard 剪贴板模块
  - [x] 6.1 创建 Clipboard Entity（Lombok 重构）
    - 创建 `com.ziv.echosync.features.clipboard.entity.Clipboard`
    - 使用 @Data + @TableName("clipboard") + @TableId(type = IdType.AUTO)
    - 包含 id、content、deviceName、createTime 字段
    - _需求: 10.2, 9.2_

  - [x] 6.2 创建 ClipboardSyncDTO
    - 创建 `com.ziv.echosync.features.clipboard.dto.ClipboardSyncDTO`
    - 包含 content 和 deviceName 字段，使用 @Data + @NotBlank 校验
    - _需求: 3.3, 3.4, 3.6_

  - [x] 6.3 创建 ClipboardVO
    - 创建 `com.ziv.echosync.features.clipboard.vo.ClipboardVO`
    - 包含 id、content、deviceName、createTime 字段，使用 @Data
    - _需求: 3.6_

  - [x] 6.4 创建 ClipboardMapper
    - 创建 `com.ziv.echosync.features.clipboard.ClipboardMapper`
    - 继承 BaseMapper\<Clipboard\>，使用 @Mapper 注解
    - _需求: 9.2_

  - [x] 6.5 创建 ClipboardService 接口和 ClipboardServiceImpl 实现
    - 创建 `com.ziv.echosync.features.clipboard.ClipboardService` 接口
    - 创建 `com.ziv.echosync.features.clipboard.ClipboardServiceImpl` 实现类
    - 实现 syncClipboard(ClipboardSyncDTO)：DTO→Entity 转换、数据库插入、Redis 缓存更新、WebSocket 广播、返回 ClipboardVO
    - 实现 getLatest()：优先读 Redis 缓存，缓存未命中查数据库并回填缓存，返回 ClipboardVO
    - _需求: 5.1, 5.2, 5.3, 5.4, 5.5, 3.7_

  - [x] 6.6 创建新的 ClipboardController
    - 创建 `com.ziv.echosync.features.clipboard.ClipboardController`
    - POST /api/clipboard/sync 接口，使用 @RequestBody + @Valid 接收 ClipboardSyncDTO，返回 Result\<ClipboardVO\>
    - GET /api/clipboard/latest 接口，返回 Result\<ClipboardVO\>
    - _需求: 5.6, 3.1, 3.2_


  - [ ]* 6.7 编写 clipboard 模块单元测试
    - 测试 DTO 空白字段校验返回 400
    - 测试 DTO → Entity → VO 字段一致性
    - **Property 3: DTO 空白字段校验**
    - **Property 4: DTO → Entity → VO 字段一致性**
    - **Property 7: 剪贴板同步操作完整性**
    - **验证: 需求 3.3, 3.4, 3.7, 5.1, 5.2, 5.3**

  - [ ] 6.8 编写 docs/03-clipboard-剪贴板模块.md 教程文档
    - 涵盖 Entity（Lombok）、DTO、VO、Mapper、Service（含 Redis 缓存逻辑）、Controller
    - 按小步骤拆分，每步包含代码示例和解释说明
    - _需求: 12.3, 12.6_

- [ ] 7. 检查点 - 确认剪贴板模块
  - 确保代码编译通过，所有测试通过，如有问题请向用户确认。

- [ ] 8. websocket 实时推送模块
  - [x] 8.1 迁移 EchoWebSocketServer 到 features/websocket 包
    - 创建 `com.ziv.echosync.features.websocket.EchoWebSocketServer`
    - 保持 @ServerEndpoint("/ws/clipboard") 和 @Component 注解
    - 维护线程安全的 CopyOnWriteArraySet\<Session\> 集合
    - broadcast 方法中对单个会话发送失败时跳过继续广播
    - _需求: 8.1, 8.2_

  - [x] 8.2 迁移 WebSocketConfig 到 features/websocket 包
    - 创建 `com.ziv.echosync.features.websocket.WebSocketConfig`
    - 注册 ServerEndpointExporter Bean
    - _需求: 8.3_

  - [ ]* 8.3 编写 WebSocket 广播容错性测试
    - **Property 9: WebSocket 广播容错性**
    - **验证: 需求 8.2**

  - [ ] 8.4 编写 docs/04-websocket-实时推送模块.md 教程文档
    - 涵盖 WebSocket 配置、EchoWebSocketServer 实现、广播机制
    - 按小步骤拆分，每步包含代码示例和解释说明
    - _需求: 12.4, 12.6_

- [ ] 9. Redis 缓存配置和集成
  - [x] 9.1 创建 RedisConfig 配置类
    - 创建 `com.ziv.echosync.common.config.RedisConfig`
    - 配置 RedisTemplate：key 使用 StringRedisSerializer，value 使用 GenericJackson2JsonRedisSerializer
    - _需求: 7.1_

  - [ ] 9.2 在 ClipboardServiceImpl 中集成 Redis 缓存逻辑
    - syncClipboard 完成后更新 clipboard:latest 缓存，TTL 5 分钟
    - getLatest 优先读缓存，缓存未命中查库并回填
    - Redis 不可用时降级为直接查数据库
    - _需求: 7.2, 7.3, 5.2, 5.4, 5.5_

  - [ ] 9.3 编写 docs/05-redis-缓存配置.md 教程文档
    - 涵盖 RedisConfig 配置、application.properties Redis 配置、缓存策略说明
    - 按小步骤拆分，每步包含代码示例和解释说明
    - _需求: 12.5, 12.6_

- [ ] 10. application.properties 更新 + 清理旧代码
  - [x] 10.1 更新 application.properties
    - 添加 Redis 连接配置（spring.data.redis.host、port、password 等）
    - 保留现有 MySQL 和 MyBatis-Plus 配置
    - _需求: 7.1_

  - [x] 10.2 删除旧的扁平结构代码文件
    - 删除旧的 `com.ziv.echosync.AuthController.java`
    - 删除旧的 `com.ziv.echosync.Clipboard.java`
    - 删除旧的 `com.ziv.echosync.ClipboardController.java`
    - 删除旧的 `com.ziv.echosync.ClipboardMapper.java`
    - 删除旧的 `com.ziv.echosync.HelloController.java`
    - 删除旧的 `com.ziv.echosync.service/` 目录
    - 删除旧的 `com.ziv.echosync.config/WebSocketConfig.java`
    - 删除旧的 `com.ziv.echosync.websocket/EchoWebSocketServer.java`
    - 确保 EchoSyncApplication.java 和 utils/JwtUtils.java 保留不动
    - _需求: 9.1, 9.2, 9.3, 9.4, 9.5_

  - [-] 10.3 创建 MyBatisPlusConfig 配置类（如需要）
    - 创建 `com.ziv.echosync.common.config.MyBatisPlusConfig`
    - 确保 @MapperScan 扫描到 features 下的 Mapper
    - _需求: 9.5_

- [ ] 11. 最终检查点 - 全面验证
  - 确保所有代码编译通过，所有测试通过，features 包结构正确，旧代码已清理。如有问题请向用户确认。

## 备注

- 标记 `*` 的子任务为可选任务，可跳过以加快 MVP 进度
- 每个任务引用了具体的需求编号，确保可追溯性
- 检查点任务确保增量验证
- Property 测试验证设计文档中定义的正确性属性
- 教程文档任务确保每个模块都有配套的学习材料
