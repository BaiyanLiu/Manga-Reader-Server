package com.baiyanliu.mangareader.messaging;

import com.baiyanliu.mangareader.configuration.WebSocketConfiguration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Date;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Message {
    @Getter private final Date timestamp = new Date();

    public void send(SimpMessagingTemplate webSocket) {
        webSocket.convertAndSend(WebSocketConfiguration.MESSAGE_PREFIX + getDestination(), this);
    }

    protected abstract String getDestination();
}
