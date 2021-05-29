package com.baiyanliu.mangareader.downloader.messaging.repository;

import com.baiyanliu.mangareader.downloader.messaging.DownloadMessage;
import com.baiyanliu.mangareader.downloader.messaging.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.logging.Level;

@Log
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Service
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
