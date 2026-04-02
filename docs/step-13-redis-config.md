# Step 13 — 创建 RedisConfig 和配置 Redis 连接

> 目标：配置 RedisTemplate 的序列化方式，并在 application.properties 中添加 Redis 连接信息。

---

## 文件位置

- `src/main/java/com/ziv/echosync/common/config/RedisConfig.java`
- `src/main/resources/application.properties`

---

## 13.1 RedisConfig

```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
```

**为什么要自定义序列化？**

Spring Boot 默认的 `RedisTemplate` 使用 JDK 序列化，存进 Redis 的数据是二进制乱码，无法用 redis-cli 直接查看。

自定义后：
- key → 字符串（如 `clipboard:latest`）
- value → JSON（如 `{"id":1,"content":"Hello",...}`）

在 redis-cli 中可以直接 `GET clipboard:latest` 看到可读的 JSON。

---

## 13.2 application.properties 添加 Redis 配置

在 `src/main/resources/application.properties` 中添加：

```properties
# Redis 连接配置
spring.data.redis.host=localhost
spring.data.redis.port=6379
# 如果 Redis 有密码，取消注释并填写
# spring.data.redis.password=your_password
```

---

## 验证 Redis 是否工作

启动项目后，调用同步接口，然后在终端查看 Redis：

```bash
redis-cli GET clipboard:latest
```

如果返回 JSON 数据，说明缓存写入成功。
