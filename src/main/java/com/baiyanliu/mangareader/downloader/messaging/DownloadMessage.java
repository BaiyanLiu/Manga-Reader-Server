package com.baiyanliu.mangareader.downloader.messaging;

import com.baiyanliu.mangareader.configuration.WebSocketConfiguration;
import com.baiyanliu.mangareader.entity.Manga;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Date;

@RequiredArgsConstructor
public abstract class DownloadMessage {
    @Getter private final Manga manga;
    @Getter private final MessageStatus status;
    @Getter private final Date timestamp = new Date();

    public void send(SimpMessagingTemplate webSocket) {
        webSocket.convertAndSend(WebSocketConfiguration.MESSAGE_PREFIX + getDestination(), this);
    }

    protected abstract String getDestination();
}
