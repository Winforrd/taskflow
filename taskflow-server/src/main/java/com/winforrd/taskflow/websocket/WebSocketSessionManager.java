package com.winforrd.taskflow.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {

    private final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 注册会话；同一用户已有旧连接时先关闭旧连接，避免孤儿会话。
     */
    public void add(Long userId, WebSocketSession session) {
        WebSocketSession old = sessions.put(userId, session);
        if (old != null && old.isOpen() && !old.getId().equals(session.getId())) {
            try {
                old.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * 仅在当前 Map 中仍是该 session 时移除，避免误删新连接。
     */
    public void remove(Long userId, WebSocketSession session) {
        if (userId == null || session == null) {
            return;
        }
        sessions.computeIfPresent(userId, (id, current) ->
                current.getId().equals(session.getId()) ? null : current
        );
    }

    public WebSocketSession get(Long userId) {
        return sessions.get(userId);
    }
}
