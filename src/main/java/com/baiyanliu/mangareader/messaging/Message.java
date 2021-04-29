package com.baiyanliu.mangareader.messaging;

import com.baiyanliu.mangareader.configuration.WebSocketConfiguration;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import javax.persistence.MappedSuperclass;
import java.util.Date;

@AllArgsConstructor
@Data
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Message {
    private Date timestamp;

    public void send(SimpMessagingTemplate webSocket) {
        webSocket.convertAndSend(WebSocketConfiguration.MESSAGE_PREFIX + getDestination(), this);
    }

    protected abstract String getDestination();
}
