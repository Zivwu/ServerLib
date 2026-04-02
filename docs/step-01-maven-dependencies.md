# Step 01 — Maven 依赖配置

> 目标：在 pom.xml 中添加 Lombok、Redis、Validation 三个依赖，为后续重构做准备。

---

## 为什么需要这三个依赖？

| 依赖 | 作用 |
|------|------|
| `lombok` | 通过注解自动生成 getter/setter/构造器，消除样板代码 |
| `spring-boot-starter-data-redis` | 提供 RedisTemplate，用于缓存最新剪贴板内容 |
| `spring-boot-starter-validation` | 提供 `@NotBlank`、`@Valid` 等注解，用于 DTO 参数校验 |

---

## 操作步骤

打开 `pom.xml`，在 `<dependencies>` 块末尾添加以下内容：

```xml
<!-- Lombok：编译期自动生成 getter/setter/构造器，不打包进 jar -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>

<!-- Redis 缓存 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- DTO 参数校验 @NotBlank / @Valid -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

然后在 `<build>` → `spring-boot-maven-plugin` 中添加 Lombok 注解处理器配置：

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

---

## 关键说明

**`<scope>provided</scope>`**
Lombok 只在编译期工作，编译完成后代码里已经有了真实的 getter/setter，运行时不再需要 Lombok 的 jar，所以用 `provided` 不打包进去。

**`annotationProcessorPaths`**
告诉 Maven 编译时要用 Lombok 的注解处理器。不配置这个，在某些 CI 环境或命令行 `mvn compile` 时可能报找不到 getter 方法的错误。

---

## 验证

在 IDEA 中点击右上角 Maven 刷新按钮，或在终端执行：

```bash
./mvnw dependency:resolve
```

没有报错即表示依赖下载成功。
