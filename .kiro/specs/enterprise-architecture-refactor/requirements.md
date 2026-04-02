# 需求文档

## 简介

EchoSync 是一个跨设备剪贴板同步应用，基于 Spring Boot 3.5 + MyBatis-Plus + WebSocket + JWT 构建。当前项目存在分层缺失、缺少统一响应封装、全局异常处理、DTO/VO 分离等问题。本次重构旨在将项目升级为标准企业级分层架构，引入 Lombok 消除样板代码，按 features 模块化组织代码，并为每个功能模块配套教程文档。

## 术语表

- **EchoSync_系统**：EchoSync 跨设备剪贴板同步后端应用
- **Controller_层**：接收 HTTP 请求、校验入参、调用 Service 并返回统一响应的组件
- **Service_层**：封装业务逻辑、协调 Mapper 和缓存操作的组件
- **Mapper_层**：基于 MyBatis-Plus 的数据库访问组件
- **Result_响应体**：统一的 API JSON 响应封装，包含 code、message、data 三个字段
- **ResultCode_枚举**：定义标准 HTTP 响应状态码和消息的枚举类
- **BusinessException**：自定义业务异常类，携带 ResultCode 信息
- **GlobalExceptionHandler**：全局异常处理器，捕获异常并返回统一 Result 响应
- **JWT_拦截器**：基于 HandlerInterceptor 的请求拦截器，校验 JWT Token 有效性
- **DTO**：Data Transfer Object，用于接收客户端请求参数的数据传输对象
- **VO**：View Object，用于向客户端返回数据的视图对象
- **Entity**：与数据库表映射的实体类
- **Redis_缓存层**：基于 Spring Data Redis 的缓存组件，用于缓存热点数据
- **WebSocket_服务**：基于 Jakarta WebSocket 的实时消息推送服务
- **Lombok**：Java 注解处理库，通过注解自动生成 getter/setter/构造器等样板代码
- **features_包结构**：按业务功能模块组织代码的包结构方式

## 需求

### 需求 1：统一响应体封装

**用户故事：** 作为后端开发者，我希望所有 API 接口返回统一的 JSON 响应格式，以便前端可以用统一的逻辑解析响应。

#### 验收标准

1. THE Result_响应体 SHALL 包含 code（整型状态码）、message（字符串消息）、data（泛型数据）三个字段
2. WHEN 业务操作成功时，THE Result_响应体 SHALL 返回 code=200 和对应的业务数据
3. WHEN 业务操作失败时，THE Result_响应体 SHALL 返回对应的错误 code 和错误 message，data 为 null
4. THE ResultCode_枚举 SHALL 定义 SUCCESS(200)、BAD_REQUEST(400)、UNAUTHORIZED(401)、FORBIDDEN(403)、NOT_FOUND(404)、INTERNAL_ERROR(500) 六个标准状态码
5. THE Result_响应体 SHALL 使用 Lombok 的 @Data 注解消除 getter/setter 样板代码
6. THE ResultCode_枚举 SHALL 使用 Lombok 的 @Getter 和 @AllArgsConstructor 注解

### 需求 2：全局异常处理

**用户故事：** 作为后端开发者，我希望系统能统一捕获和处理异常，以便所有异常都以统一的 Result 格式返回给客户端。

#### 验收标准

1. WHEN BusinessException 被抛出时，THE GlobalExceptionHandler SHALL 捕获该异常并返回包含对应 ResultCode 的 Result 响应
2. WHEN MethodArgumentNotValidException 被抛出时，THE GlobalExceptionHandler SHALL 捕获该异常并返回 code=400 的 Result 响应，message 包含具体校验失败信息
3. WHEN 未预期的 Exception 被抛出时，THE GlobalExceptionHandler SHALL 捕获该异常并返回 code=500 的 Result 响应
4. THE BusinessException SHALL 携带 ResultCode 字段，使用 Lombok 的 @Getter 注解
5. THE GlobalExceptionHandler SHALL 使用 @RestControllerAdvice 注解标记

### 需求 3：DTO/VO 分离与参数校验

**用户故事：** 作为后端开发者，我希望请求入参和响应出参使用独立的 DTO 和 VO 对象，以便实现 Controller 层与 Entity 的解耦。

#### 验收标准

1. THE Controller_层 SHALL 使用 DTO 对象接收请求参数，禁止直接接收 Entity 对象
2. THE Controller_层 SHALL 使用 Result\<VO\> 作为返回类型，禁止直接返回 Entity 对象
3. WHEN ClipboardSyncDTO 的 content 字段为空白时，THE EchoSync_系统 SHALL 返回 code=400 的校验失败响应
4. WHEN ClipboardSyncDTO 的 deviceName 字段为空白时，THE EchoSync_系统 SHALL 返回 code=400 的校验失败响应
5. WHEN AuthLoginDTO 的 deviceName 字段为空白时，THE EchoSync_系统 SHALL 返回 code=400 的校验失败响应
6. THE DTO 和 VO 类 SHALL 使用 Lombok 的 @Data 注解消除样板代码
7. THE Service_层 SHALL 负责 DTO 到 Entity 的转换以及 Entity 到 VO 的转换

### 需求 4：JWT 认证拦截

**用户故事：** 作为后端开发者，我希望非公开接口都经过 JWT Token 校验，以便只有合法设备才能访问受保护的资源。

#### 验收标准

1. WHEN 请求路径不在白名单中且请求头缺少 Authorization 字段时，THE JWT_拦截器 SHALL 抛出 BusinessException(UNAUTHORIZED)
2. WHEN 请求头中的 JWT Token 无效或已过期时，THE JWT_拦截器 SHALL 抛出 BusinessException(UNAUTHORIZED)
3. WHEN JWT Token 校验成功时，THE JWT_拦截器 SHALL 将解析出的设备名存入 HttpServletRequest 的 attribute 中并放行请求
4. THE WebMvcConfig SHALL 将 /api/auth/login 路径注册为拦截器白名单
5. THE JWT_拦截器 SHALL 从请求头 Authorization 字段中提取 "Bearer " 前缀后的 Token 字符串

### 需求 5：剪贴板同步功能重构

**用户故事：** 作为用户，我希望在一台设备上复制的内容能同步到其他设备，以便实现跨设备剪贴板共享。

#### 验收标准

1. WHEN 客户端发送有效的 ClipboardSyncDTO 到 POST /api/clipboard/sync 时，THE ClipboardService SHALL 将内容持久化到数据库
2. WHEN 剪贴板记录成功持久化后，THE ClipboardService SHALL 更新 Redis 缓存键 clipboard:latest 并设置 5 分钟 TTL
3. WHEN 剪贴板记录成功持久化后，THE WebSocket_服务 SHALL 向所有在线会话广播该记录的 JSON 数据
4. WHEN 客户端请求 GET /api/clipboard/latest 时，THE ClipboardService SHALL 优先从 Redis 缓存读取最新记录
5. WHEN Redis 缓存未命中时，THE ClipboardService SHALL 从数据库查询最新记录并回填到 Redis 缓存中
6. THE ClipboardController SHALL 返回 Result\<ClipboardVO\> 类型的统一响应

### 需求 6：设备认证功能重构

**用户故事：** 作为用户，我希望通过设备名登录获取 Token，以便后续请求能通过 JWT 认证。

#### 验收标准

1. WHEN 客户端发送有效的 AuthLoginDTO 到 POST /api/auth/login 时，THE AuthService SHALL 调用 JwtUtils 生成 JWT Token
2. WHEN Token 生成成功时，THE AuthController SHALL 返回 Result\<AuthTokenVO\> 包含 token 和 deviceName
3. THE AuthLoginDTO SHALL 使用 @RequestBody 接收 JSON 格式的请求体，替代原有的 @RequestParam 方式

### 需求 7：Redis 缓存集成

**用户故事：** 作为后端开发者，我希望引入 Redis 缓存层，以便减少数据库查询压力并提升响应速度。

#### 验收标准

1. THE RedisConfig SHALL 配置 RedisTemplate 的序列化方式，key 使用 StringRedisSerializer，value 使用 GenericJackson2JsonRedisSerializer
2. WHEN 剪贴板同步操作完成后，THE Redis_缓存层 SHALL 以 clipboard:latest 为键缓存最新记录，TTL 为 5 分钟
3. IF Redis 服务不可用，THEN THE ClipboardService SHALL 降级为直接查询数据库，保证功能可用

### 需求 8：WebSocket 实时推送重构

**用户故事：** 作为用户，我希望在其他设备同步剪贴板时能实时收到推送，以便及时获取最新内容。

#### 验收标准

1. THE WebSocket_服务 SHALL 维护所有在线会话的线程安全集合
2. WHEN 某个 WebSocket 会话发送消息失败时，THE WebSocket_服务 SHALL 跳过该会话继续向其他会话广播
3. THE WebSocketConfig SHALL 配置在 features/websocket 包下，注册 ServerEndpointExporter Bean

### 需求 9：features 模块化包结构

**用户故事：** 作为后端开发者，我希望代码按业务功能模块组织在 features 包下，以便提高代码的可维护性和可读性。

#### 验收标准

1. THE EchoSync_系统 SHALL 将认证相关代码组织在 com.ziv.echosync.features.auth 包下
2. THE EchoSync_系统 SHALL 将剪贴板相关代码组织在 com.ziv.echosync.features.clipboard 包下
3. THE EchoSync_系统 SHALL 将 WebSocket 相关代码组织在 com.ziv.echosync.features.websocket 包下
4. THE EchoSync_系统 SHALL 将公共基础设施代码组织在 com.ziv.echosync.common 包下，包含 result、exception、config 子包
5. THE EchoSync_系统 SHALL 保持 Controller → Service → Mapper 的单向依赖，禁止反向依赖

### 需求 10：Lombok 集成

**用户故事：** 作为后端开发者，我希望使用 Lombok 消除所有样板代码，以便代码更简洁、更易维护。

#### 验收标准

1. THE pom.xml SHALL 包含 Lombok 依赖配置
2. THE Entity 类 SHALL 使用 @Data 注解替代手写的 getter/setter/toString/equals/hashCode 方法
3. THE DTO 类 SHALL 使用 @Data 注解
4. THE VO 类 SHALL 使用 @Data 注解
5. THE BusinessException SHALL 使用 Lombok 注解（@Getter）替代手写 getter

### 需求 11：Maven 依赖管理

**用户故事：** 作为后端开发者，我希望项目引入必要的新依赖，以便支持 Redis 缓存、参数校验和 Lombok 功能。

#### 验收标准

1. THE pom.xml SHALL 新增 spring-boot-starter-data-redis 依赖
2. THE pom.xml SHALL 新增 spring-boot-starter-validation 依赖
3. THE pom.xml SHALL 新增 lombok 依赖并配置为 provided scope
4. THE pom.xml SHALL 保留现有的 spring-boot-starter-web、spring-boot-starter-websocket、mybatis-plus-spring-boot3-starter、mysql-connector-j、jjwt 依赖

### 需求 12：教程文档配套

**用户故事：** 作为学习者，我希望每个功能模块都有配套的教程文档，以便按小步骤学习和理解每个模块的实现。

#### 验收标准

1. THE EchoSync_系统 SHALL 在 docs/ 目录下为 common 基础设施模块提供教程文档，涵盖 Lombok 配置、Result、ResultCode、BusinessException、GlobalExceptionHandler
2. THE EchoSync_系统 SHALL 在 docs/ 目录下为 auth 认证模块提供教程文档，涵盖 DTO、VO、Service、Controller、JwtInterceptor
3. THE EchoSync_系统 SHALL 在 docs/ 目录下为 clipboard 剪贴板模块提供教程文档，涵盖 Entity、DTO、VO、Mapper、Service（含 Redis 缓存）、Controller
4. THE EchoSync_系统 SHALL 在 docs/ 目录下为 websocket 实时推送模块提供教程文档
5. THE EchoSync_系统 SHALL 在 docs/ 目录下为 Redis 缓存配置提供教程文档
6. WHEN 编写教程文档时，THE 教程文档 SHALL 将每个功能拆分为多个小步骤，每步包含代码示例和解释说明
