package com.winforrd.taskflow.websocket;


import lombok.RequiredArgsConstructor;


import org.springframework.context.annotation.Configuration;


import org.springframework.web.socket.config.annotation.*;



@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig
        implements WebSocketConfigurer {



    private final NotificationWebSocketHandler handler;




    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry){

        registry.addHandler(handler, "/ws/notification").setAllowedOrigins("*");


    }


}