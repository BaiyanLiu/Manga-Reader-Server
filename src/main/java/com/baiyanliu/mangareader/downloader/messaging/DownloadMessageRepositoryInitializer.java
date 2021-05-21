package com.baiyanliu.mangareader.downloader.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.logging.Level;

@Component
@Log
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class DownloadMessageRepositoryInitializer {
    private final DownloadMessageRepository repository;

    @PostConstruct
    public void cancelOrphanMessages() {
        log.log(Level.INFO, "cancelOrphanMessages");
        Iterable<DownloadMessage> messages = repository.findAllByStatus(Status.STARTED);
        for (DownloadMessage message : messages) {
            log.log(Level.INFO, String.format("cancelOrphanMessages - message [%d]", message.getId()));
            message.setStatus(Status.CANCELLED);
        }
        repository.saveAll(messages);
    }
}
