package com.ziv.echosync.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ws/clipboard") // 定义客户端连接的 URL 路径
@Component
public class EchoWebSocketServer {

    // 用一个线程安全的集合，保存所有当前连接的设备会话
    // 注意这里必须是 static 的，因为每次有新连接，Tomcat 都会创建一个新的 EchoWebSocketServer 实例
    private static final CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<>();

    @OnOpen // 当有设备连接成功时触发
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("🟢 新设备连接成功！当前在线设备数: " + sessions.size());
    }

    @OnClose // 当设备断开连接时触发
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("🔴 设备断开连接。当前在线设备数: " + sessions.size());
    }

    @OnMessage // 当收到客户端发来的消息时触发（目前我们主要用服务器下发，这个做预留）
    public void onMessage(String message, Session session) {
        System.out.println("收到设备消息: " + message);
    }

    @OnError // 发生错误时触发
    public void onError(Session session, Throwable error) {
        System.out.println("WebSocket 发生错误: " + error.getMessage());
    }

    /**
     * 核心方法：向所有在线设备广播消息
     */
    public static void broadcast(String message) {
        for (Session session : sessions) {
            try {
                // 异步发送文本消息给客户端
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}