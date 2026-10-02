package com.winforrd.taskflow.websocket;


import org.springframework.stereotype.Component;

import org.springframework.web.socket.WebSocketSession;


import java.util.concurrent.ConcurrentHashMap;



@Component
public class WebSocketSessionManager {

    private final ConcurrentHashMap<Long,WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void add(Long userId, WebSocketSession session){
        sessions.put(userId, session);
    }

    public void remove(Long userId){
        sessions.remove(userId);
    }

    public WebSocketSession get(Long userId){
        return sessions.get(userId);
    }
}