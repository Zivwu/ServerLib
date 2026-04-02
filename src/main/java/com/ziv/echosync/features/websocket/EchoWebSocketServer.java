package com.ziv.echosync.features.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket 服务端
 * 维护所有在线设备的会话，提供广播能力
 */
@ServerEndpoint("/ws/clipboard")
@Component
public class EchoWebSocketServer {

    // 线程安全的会话集合（static：所有实例共享同一个集合）
    private static final CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("新设备连接，当前在线: " + sessions.size());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("设备断开，当前在线: " + sessions.size());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("收到消息: " + message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket 错误: " + error.getMessage());
    }

    /**
     * 向所有在线设备广播消息
     * 单个会话发送失败时跳过，不影响其他会话
     */
    public static void broadcast(String message) {
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                System.err.println("向会话 " + session.getId() + " 发送失败，跳过: " + e.getMessage());
            }
        }
    }
}
