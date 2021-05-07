package com.baiyanliu.mangareader.messaging;

import com.baiyanliu.mangareader.configuration.WebSocketConfiguration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public abstract class Message {

    public void send(SimpMessagingTemplate webSocket) {
        webSocket.convertAndSend(WebSocketConfiguration.MESSAGE_PREFIX + getDestination(), this);
    }

    protected abstract String getDestination();
}
