# Step 15 — 从教学 Demo 升级到模块化单体

> 目标：把当前项目从“能跑的教学 Demo”升级为“结构清晰、配置安全、能测试、能扩展的模块化单体”。
>
> 这一步**不是**让你把项目改成微服务，也**不是**一次性引入大量复杂框架，而是按真实企业项目的最小必要标准，逐步把基础打稳。

---

## 这份文档适合谁

如果你当前的目标是下面这几件事，这份文档就是给你准备的：

- 你已经完成了当前 `features/` 分模块版本；
- 你希望项目更像真实公司里的 Java 后端；
- 你想继续练习架构，但不希望一上来就被微服务、DDD 全套、DevOps 工具链压垮；
- 你更希望“边改边学”，每一步都知道为什么。

---

## 先说结论：这次升级要做什么

当前项目已经有了基础模块划分：

- `common/`
- `features/auth/`
- `features/clipboard/`
- `features/websocket/`

这已经比最开始的扁平结构清晰很多，但还存在几个典型的 Demo 问题：

1. 业务类职责过重。
2. 配置和密钥写死在代码里。
3. 缺少测试。
4. 基础设施能力直接耦合到业务代码。
5. 缺少更稳定的模块内分层。
6. 缺少日志、环境隔离、统一配置治理。

所以这一步要完成的，不是“推翻重写”，而是下面 6 个升级目标：

1. 重新整理模块内部结构。
2. 把敏感配置移出代码。
3. 给项目补上基础测试。
4. 让业务层不直接依赖太多基础设施细节。
5. 增加日志、环境、错误处理等工程能力。
6. 为未来扩展留下清晰边界。

---

## 升级后的目标结构

这次升级后，不要求你立刻把每个模块都拆到最细，但目录结构要开始往下面这个方向靠：

```text
com.ziv.echosync
├── EchoSyncApplication.java
├── common
│   ├── config
│   ├── exception
│   ├── result
│   ├── security
│   ├── logging
│   └── support
├── modules
│   ├── auth
│   │   ├── api
│   │   ├── application
│   │   ├── domain
│   │   └── infrastructure
│   ├── clipboard
│   │   ├── api
│   │   ├── application
│   │   ├── domain
│   │   └── infrastructure
│   └── websocket
│       ├── api
│       ├── application
│       └── infrastructure
└── shared
    └── utils
```

你**不必一次性把全部代码都迁过去**。学习项目最好的方式是“先建立规则，再逐步搬迁”。

因此建议你采用下面这种渐进式迁移策略：

- 第一阶段：保留 `features/` 不动，只优化内部职责。
- 第二阶段：当你理解了分层，再把 `features` 重命名为 `modules`。
- 第三阶段：把每个模块内部拆成 `api / application / domain / infrastructure`。

如果你现在直接把包名全部改掉，学习收益不一定最高，反而容易陷入机械重构。

---

## 这次升级的推荐顺序

请严格按照下面顺序来，不建议跳步：

1. 配置安全化
2. 日志替换 `System.out`
3. 模块内部职责拆分
4. 提取基础设施接口
5. 补测试
6. 增加工程化能力
7. 最后再考虑包结构升级

原因很简单：

- 先做配置安全化，项目才像“能放到真实环境”；
- 先做日志，后面排错才容易；
- 先拆职责，再写测试，测试边界更清晰；
- 最后再搬包，不容易改乱。

---

# 第一部分：配置安全化

## 1.1 为什么第一步必须先做配置

你当前项目里有两个明显的风险：

- 数据库账号密码写在 `application.properties`
- JWT 密钥写在 `JwtUtils` 代码里

这在教学阶段可以接受，但在真实项目里属于必须优先清理的问题。

企业项目最基本的配置原则是：

- 代码里不写死密码和密钥；
- 不同环境使用不同配置；
- 敏感配置优先通过环境变量注入。

---

## 1.2 你要改成什么样

把当前单个配置文件：

- `src/main/resources/application.properties`

拆成：

- `application.yml`
- `application-dev.yml`
- `application-test.yml`
- `application-prod.yml`

建议结构如下：

```yaml
# application.yml
spring:
  application:
    name: EchoSync
  profiles:
    active: dev
```

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/echosync?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

app:
  security:
    jwt-secret: ${JWT_SECRET:change-me-in-dev}
    jwt-expire-ms: 604800000
```

```yaml
# application-prod.yml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD:}

app:
  security:
    jwt-secret: ${JWT_SECRET}
    jwt-expire-ms: ${JWT_EXPIRE_MS:604800000}
```

---

## 1.3 实际操作步骤

### 第一步：把 `application.properties` 改成 `application.yml`

你可以把通用配置先迁进去，例如：

- 应用名
- 当前 profile
- MyBatis-Plus 日志开关是否默认启用

注意：

- 开发环境可以打开 SQL 日志；
- 生产环境默认不要直接打印 SQL 到控制台。

### 第二步：新建 `application-dev.yml`

把你当前本地开发需要的配置放进去：

- MySQL
- Redis
- JWT

### 第三步：新建 `application-prod.yml`

只保留从环境变量读取的方式，不要给真实默认密码。

### 第四步：给 JWT 单独写配置类

当前 `JwtUtils` 是工具类硬编码密钥。升级后建议改成：

- `common/security/JwtProperties.java`
- `common/security/JwtService.java`

职责分别是：

- `JwtProperties`：读取配置；
- `JwtService`：负责生成和解析 token。

不要再用静态工具类保存密钥。

---

## 1.4 这一阶段的验收标准

完成后你要检查这 5 件事：

1. 仓库里不再出现真实密码和真实密钥。
2. JWT 密钥来自配置，不在 Java 代码里硬编码。
3. 本地启动默认读取 `dev` 配置。
4. 切换到 `prod` 时必须依赖环境变量。
5. 项目仍然能够正常启动。

---

# 第二部分：日志替换 `System.out`

## 2.1 为什么必须做

现在项目里像 WebSocket、Redis 异常这些地方，还是在用：

- `System.out.println`
- `System.err.println`

这在学习阶段能看到输出，但它有几个问题：

- 无法区分日志级别；
- 不方便统一搜索；
- 不便于后续接 ELK、Graylog、云日志平台；
- 无法带 traceId、线程名、类名等上下文。

企业项目里，日志不是“打印内容”，而是“排查系统行为的证据”。

---

## 2.2 你要改成什么样

统一使用 `slf4j + logback`。

Spring Boot 已经内置日志能力，所以你不需要额外引入复杂依赖。你只要：

1. 在类里声明 logger
2. 把 `System.out.println` 替换掉

示例写法：

```java
private static final Logger log = LoggerFactory.getLogger(EchoWebSocketServer.class);
```

然后替换成：

```java
log.info("新设备连接，当前在线: {}", sessions.size());
log.error("WebSocket 错误", error);
```

---

## 2.3 优先改哪些类

建议优先改这几个位置：

- `features/websocket/EchoWebSocketServer`
- `features/clipboard/ClipboardServiceImpl`
- `common/exception/GlobalExceptionHandler`

原因：

- WebSocket 和缓存问题最难排查；
- 异常处理器是所有接口失败时的总入口。

---

## 2.4 这一阶段的验收标准

1. 项目里不再使用 `System.out.println` 和 `System.err.println`。
2. 关键路径日志至少有 `info / warn / error` 基本分级。
3. 抛异常时日志中能看到堆栈，而不是只有一句字符串。

---

# 第三部分：模块内部职责拆分

## 3.1 当前最大问题是什么

你现在最典型的问题是：`ClipboardServiceImpl` 负责太多事情。

它同时在做：

1. DTO 转 Entity
2. 数据库存储
3. Redis 缓存
4. WebSocket 广播
5. Entity 转 VO

这在 Demo 阶段很常见，但在真实项目中，后续任何一个点变化都会影响整个类。

例如：

- 缓存策略变了；
- 推送方式从 WebSocket 改成 MQ；
- 持久化表结构变了；
- 对外返回字段变了；

都会让这个类继续膨胀。

---

## 3.2 正确的拆法是什么

对当前 `clipboard` 模块，建议先拆成下面 5 个角色：

### 1. Controller

负责：

- 接收请求
- 参数校验
- 返回响应

不负责：

- 核心业务流程
- 调缓存
- 调数据库

### 2. Application Service

例如：

- `ClipboardApplicationService`

负责：

- 编排“同步剪贴板”这个完整用例；
- 调用 repository 保存；
- 调用 cache 更新；
- 调用 publisher 广播；

它是整个业务用例的总调度者。

### 3. Domain Model

例如：

- `ClipboardRecord`

负责：

- 表达核心业务对象；
- 承载领域上的基础规则。

当前项目领域很轻，不必强行做很重的领域建模，但至少要意识到：

- 数据库 Entity 不等于领域对象；
- 对外 VO 也不等于领域对象。

### 4. Infrastructure

例如：

- `ClipboardRepositoryImpl`
- `ClipboardCacheRepository`
- `ClipboardMessagePublisher`

负责：

- MyBatis
- Redis
- WebSocket

这部分是技术实现细节，不应该直接塞进应用服务里。

### 5. Assembler / Converter

负责：

- DTO -> Command
- Domain -> VO
- Entity -> Domain

转换逻辑抽出来后，服务类会清爽很多。

---

## 3.3 对当前项目的实际建议

你不需要一下子改成很重的 DDD 风格。先做一个“简化版企业分层”就够了。

推荐 `clipboard` 模块先整理成：

```text
features/clipboard
├── api
│   └── ClipboardController.java
├── application
│   ├── ClipboardApplicationService.java
│   └── ClipboardAssembler.java
├── domain
│   ├── ClipboardRecord.java
│   ├── ClipboardRepository.java
│   └── ClipboardPublisher.java
└── infrastructure
    ├── persistence
    │   ├── ClipboardEntity.java
    │   ├── ClipboardMapper.java
    │   └── ClipboardRepositoryImpl.java
    ├── cache
    │   └── ClipboardCacheService.java
    └── websocket
        └── WebSocketClipboardPublisher.java
```

如果你觉得这一步太大，可以先采用“半步拆分”：

```text
features/clipboard
├── ClipboardController.java
├── ClipboardApplicationService.java
├── ClipboardRepository.java
├── ClipboardCacheService.java
├── ClipboardPublisher.java
├── ClipboardMapper.java
├── dto/
├── entity/
└── vo/
```

这个版本更适合学习。

---

## 3.4 推荐你先从哪个模块下手

先改 `clipboard`，不要先改 `auth`。

原因：

- `clipboard` 逻辑最复杂；
- 它同时涉及 DB、Redis、WebSocket；
- 改好这个模块后，你会真正理解“职责拆分”的意义。

`auth` 模块目前逻辑非常轻，暂时收益没那么高。

---

## 3.5 这一阶段的验收标准

1. `ClipboardServiceImpl` 这种“大而全”的类被拆小。
2. 控制器不再直接包含复杂业务逻辑。
3. 缓存、广播、持久化都有相对独立的职责类。
4. 代码阅读时能明显看出“谁负责什么”。

---

# 第四部分：提取基础设施接口，降低耦合

## 4.1 这一步是很多学习项目最容易忽略的

你现在的业务代码是直接使用：

- `ClipboardMapper`
- `RedisTemplate`
- `EchoWebSocketServer.broadcast`

这会导致一个问题：

业务逻辑和技术细节绑死了。

例如以后你想：

- 把 Redis 改成 Caffeine；
- 把 WebSocket 广播换成消息队列；
- 把 MyBatis 改成 JPA；

你会发现业务类也必须跟着改。

---

## 4.2 正确做法

在业务层面只依赖接口，不依赖具体实现。

例如：

```java
public interface ClipboardRepository {
    ClipboardRecord save(ClipboardRecord record);
    Optional<ClipboardRecord> findLatest();
}
```

```java
public interface ClipboardPublisher {
    void publishClipboardSynced(ClipboardRecord record);
}
```

```java
public interface ClipboardCache {
    void cacheLatest(ClipboardRecord record);
    Optional<ClipboardRecord> getLatest();
}
```

然后在 `infrastructure` 层给出实现：

- `MybatisClipboardRepository`
- `RedisClipboardCache`
- `WebSocketClipboardPublisher`

---

## 4.3 这一步的学习重点

这里最重要的不是“接口越多越高级”，而是理解一句话：

**应用层依赖抽象，基础设施层依赖技术实现。**

这句话是企业项目可扩展性的核心之一。

---

## 4.4 这一阶段的验收标准

1. 应用服务不再直接依赖 `RedisTemplate`。
2. 应用服务不再直接调用 `EchoWebSocketServer.broadcast()`。
3. 应用服务主要依赖自己定义的接口。

---

# 第五部分：补测试，让项目真正“可维护”

## 5.1 为什么教学项目最容易烂在这里

很多学习项目写到能跑就结束了，没有测试。结果是：

- 改一点代码就怕把别的逻辑弄坏；
- 重构时没有安全感；
- 面试时也很难证明自己理解了工程化。

企业项目不一定测试 100% 覆盖，但核心路径必须能验证。

---

## 5.2 当前项目最应该补哪几类测试

建议按下面顺序补：

### 第一类：Controller 集成测试

目标：

- 验证接口返回结构；
- 验证参数校验；
- 验证异常处理；
- 验证未登录访问是否被拦截。

优先测试接口：

- `/api/auth/login`
- `/api/clipboard/sync`
- `/api/clipboard/latest`

建议工具：

- `spring-boot-starter-test`
- `MockMvc`

### 第二类：Application Service 单元测试

目标：

- 验证业务编排逻辑是否正确；
- 验证缓存命中/回退逻辑；
- 验证广播是否在正确时机触发。

建议针对：

- `ClipboardApplicationService`

### 第三类：Repository 持久化测试

目标：

- 验证 mapper SQL 是否正确；
- 验证 `findLatest()` 之类查询逻辑。

可以后续补，不必第一天完成。

---

## 5.3 建议你先写的 6 个测试点

这 6 个测试最实用：

1. 登录成功时返回 token。
2. 缺少 `deviceName` 时登录接口返回参数错误。
3. 未带 token 访问 `/api/clipboard/latest` 返回未授权。
4. 正常同步剪贴板时返回成功结果。
5. 获取最新剪贴板时，缓存命中能返回数据。
6. 缓存异常时，服务能降级到数据库。

只要你把这 6 个用例写完，项目的可靠性就已经明显提升。

---

## 5.4 测试目录建议

```text
src/test/java/com/ziv/echosync
├── auth
│   └── AuthControllerTest.java
├── clipboard
│   ├── ClipboardControllerTest.java
│   └── ClipboardApplicationServiceTest.java
└── support
    └── TestJwtFactory.java
```

---

## 5.5 这一阶段的验收标准

1. 核心接口至少有基础测试。
2. 重构后能通过测试验证主要行为没有回归。
3. 你能用测试证明“项目不是只能手工点点看”。

---

# 第六部分：补工程化能力

## 6.1 这一部分不是“锦上添花”，而是“像企业项目”的标志

如果说前面几步是在做代码结构升级，那么这一步是在做工程能力升级。

建议你优先补下面 5 个能力。

---

## 6.2 能力一：统一日志格式

建议加：

- 时间
- 日志级别
- 线程名
- 类名
- 消息

后续如果你愿意，可以继续加：

- traceId
- requestId
- userId / deviceName

---

## 6.3 能力二：全局请求追踪

建议增加一个 `Filter` 或 `Interceptor`：

- 为每个请求生成 traceId；
- 放入 MDC；
- 日志自动带上 traceId。

这样之后查线上问题时，不同日志可以串起来看。

---

## 6.4 能力三：健康检查

建议引入：

- Spring Boot Actuator

最基础至少暴露：

- `health`
- `info`

这样可以知道：

- 应用是否活着；
- Redis / DB 是否可连。

---

## 6.5 能力四：统一时间和序列化规范

建议统一这些规则：

- 日期时间输出格式；
- 时区；
- JSON 序列化策略；
- Long 是否转字符串；

这样前后端联调时会省很多麻烦。

---

## 6.6 能力五：统一错误码规范

你已经有 `ResultCode`，这是好的开始。

后续建议把错误码分层：

- 通用错误
- 参数错误
- 认证错误
- 业务错误
- 系统错误

例如：

```text
1000-1999 通用错误
2000-2999 参数错误
3000-3999 认证授权错误
4000-4999 业务错误
5000-5999 系统错误
```

这会让你的错误码体系更像真实项目。

---

## 6.7 这一阶段的验收标准

1. 项目启动后能看见规范日志。
2. 请求日志里可以追踪同一次调用。
3. 健康检查接口可用。
4. 错误码体系更清晰。

---

# 第七部分：最后再升级包结构

## 7.1 为什么不建议一开始就改包结构

很多人一上来就想把目录改成特别“高级”的样子，但如果职责还没拆清楚，改包名只是表面工程。

正确顺序应该是：

1. 先把职责拆出来；
2. 再把这些类放进对应层级；
3. 最后再把 `features` 升级成更清晰的 `modules`。

---

## 7.2 什么时候可以开始迁移

当你满足下面条件时，就可以开始做包迁移：

1. `clipboard` 模块已经有 `controller + application + repository/cache/publisher` 这类职责拆分；
2. 你已经理解每一层做什么；
3. 测试已经补上一部分；
4. 项目改动后可以快速验证。

---

## 7.3 迁移建议

建议按模块逐个迁移，不要一次性全项目大搬家。

推荐顺序：

1. `clipboard`
2. `auth`
3. `websocket`

每迁移一个模块，就做一次完整自测。

---

# 最终推荐的实施计划

如果你希望整个学习过程更稳，我建议你用下面这个 4 周节奏来推进。

---

## 第 1 周：配置和日志

本周目标：

- 拆配置文件；
- JWT 密钥移出代码；
- 用环境变量读取敏感配置；
- 替换 `System.out`；
- 跑通项目。

完成标志：

- 项目仍然能启动；
- 本地接口可正常调用；
- 仓库里不再有真实密钥和密码。

---

## 第 2 周：拆 `clipboard` 模块职责

本周目标：

- 拆出应用服务；
- 拆出 repository / cache / publisher；
- 减少大类；
- 明确职责边界。

完成标志：

- 代码阅读时，一眼能看出流程和职责；
- `ClipboardServiceImpl` 不再是“大杂烩”。

---

## 第 3 周：补测试

本周目标：

- 补登录接口测试；
- 补鉴权测试；
- 补剪贴板同步/查询测试；
- 补至少 1 个应用服务测试。

完成标志：

- 关键接口和核心流程有自动化验证。

---

## 第 4 周：工程化和包迁移

本周目标：

- 引入健康检查；
- 整理错误码；
- 视情况迁移 `features` -> `modules`；
- 梳理最终目录。

完成标志：

- 项目结构和工程能力已经明显脱离“教学 demo”。

---

# 你在实现过程中要特别注意的 8 个原则

1. 不要一开始就追求复杂架构。
2. 不要为了“企业级”而引入过多抽象。
3. 每一步都要保证项目还能启动。
4. 每做完一个阶段就提交一次代码。
5. 先重构 `clipboard`，再动其他模块。
6. 先写最有价值的测试，不要追求全覆盖。
7. 优先解决配置安全、日志、边界问题。
8. 学习项目的重点是“理解为什么这样设计”，不是目录越多越高级。

---

# 建议你现在立刻开始的第一批任务

如果你要开始动手，最推荐的起步清单就是这 7 项：

1. 把 `application.properties` 拆成多环境 `yml`。
2. 把 JWT 密钥从 `JwtUtils` 中移除，改成配置注入。
3. 新建 `JwtProperties` 和 `JwtService`。
4. 把所有 `System.out.println` 换成 logger。
5. 新建 `ClipboardApplicationService`。
6. 抽出 `ClipboardPublisher` 和 `ClipboardCache`。
7. 写 `AuthControllerTest` 和 `ClipboardControllerTest`。

如果你只做完这 7 项，项目就已经从“课程作业级别”提升到“有工程意识的学习项目”了。

---

# 最后的建议

这次升级的核心不是“把项目做大”，而是“把项目做稳”。

你现在最需要学会的，不是微服务拆分，也不是复杂 DDD，而是下面 4 件事：

1. 怎么控制职责边界；
2. 怎么管理配置和密钥；
3. 怎么用测试保护重构；
4. 怎么让项目具备最基础的工程化能力。

只要你把这 4 件事做好，这个项目就已经非常适合拿来学习真实后端架构了。

后续如果你愿意，可以继续基于这份文档做 Step 16、Step 17：

- Step 16：JWT 配置化 + 安全层重构
- Step 17：Clipboard 模块职责拆分
- Step 18：测试体系搭建

这样会形成一套连续的学习路线，而不是一次性大改。
