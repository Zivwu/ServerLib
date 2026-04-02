# Step 12 — 迁移 WebSocket 模块到 features/websocket

> 目标：将 WebSocket 相关代码迁移到 features/websocket 包，并增强广播的容错性。

---

## 文件位置

```
features/websocket/
├── EchoWebSocketServer.java  # WebSocket 服务端
└── WebSocketConfig.java      # 配置类
```

---

## EchoWebSocketServer

```java
@ServerEndpoint("/ws/clipboard")
@Component
public class EchoWebSocketServer {

    private static final CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(Session session) { sessions.add(session); }

    @OnClose
    public void onClose(Session session) { sessions.remove(session); }

    @OnMessage
    public void onMessage(String message, Session session) { }

    @OnError
    public void onError(Session session, Throwable error) { }

    public static void broadcast(String message) {
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                // 单个会话失败，跳过继续广播其他会话
                System.err.println("发送失败，跳过: " + e.getMessage());
            }
        }
    }
}
```

**`CopyOnWriteArraySet`**
线程安全的集合。每次写操作（add/remove）都会复制一份新数组，读操作不加锁，适合读多写少的场景（大量广播，少量连接/断开）。

**`static sessions`**
必须是 static，因为每次有新连接，Tomcat 都会创建一个新的 `EchoWebSocketServer` 实例，所有实例需要共享同一个会话集合。

---

## WebSocketConfig

```java
@Configuration
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
```

`ServerEndpointExporter` 是 Spring 提供的 Bean，负责扫描并注册所有 `@ServerEndpoint` 注解的类。没有它，WebSocket 端点不会生效。

---

## 客户端连接方式

```javascript
// JavaScript 客户端
const ws = new WebSocket('ws://localhost:8080/ws/clipboard');

ws.onmessage = (event) => {
    const clipboard = JSON.parse(event.data);
    console.log('收到新剪贴板:', clipboard.content);
};
```
