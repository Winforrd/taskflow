package com.winforrd.taskflow.websocket;


import lombok.RequiredArgsConstructor;


import org.springframework.stereotype.Component;


import org.springframework.web.socket.*;


import org.springframework.web.socket.handler.TextWebSocketHandler;



@Component
@RequiredArgsConstructor
public class NotificationWebSocketHandler
        extends TextWebSocketHandler {



    private final WebSocketSessionManager manager;




    @Override
    public void afterConnectionEstablished(
            WebSocketSession session
    ){


        Long userId =
                Long.valueOf(
                        session.getUri()
                                .getQuery()
                                .replace(
                                        "userId=",
                                        ""
                                )
                );


        manager.add(
                userId,
                session
        );


    }





    public void send(Long userId, String message){
        WebSocketSession session = manager.get(userId);
        if(session!=null && session.isOpen()){

            try{

                session.sendMessage(
                        new TextMessage(message)
                );


            }catch(Exception e){

                e.printStackTrace();

            }

        }


    }





    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status
    ){

    }


}