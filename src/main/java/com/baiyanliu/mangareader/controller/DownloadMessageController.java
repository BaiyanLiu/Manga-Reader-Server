package com.baiyanliu.mangareader.controller;

import com.baiyanliu.mangareader.downloader.TaskManager;
import com.baiyanliu.mangareader.downloader.messaging.DownloadMessageHelper;
import com.baiyanliu.mangareader.downloader.messaging.Status;
import com.baiyanliu.mangareader.downloader.messaging.repository.DownloadMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;

@Log
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class DownloadMessageController {
    private final DownloadMessageRepository downloadMessageRepository;
    private final TaskManager taskManager;
    private final DownloadMessageHelper downloadMessageHelper;

    @RequestMapping("/cancelDownload/{id}")
    public ResponseEntity<Void> cancelDownload(@PathVariable("id") Long messageId) {
        log.log(Level.INFO, String.format("cancelDownload - id [%d]", messageId));
        taskManager.cancelTask(messageId);
        return ResponseEntity.ok().build();
    }

    @RequestMapping("/cancelAll")
    public ResponseEntity<Void> cancelAll() {
        log.log(Level.INFO, "cancelAll");
        taskManager.cancelAll();
        return ResponseEntity.ok().build();
    }

    @RequestMapping("/resolveError/{id}")
    public ResponseEntity<Void> resolveError(@PathVariable("id") Long messageId) {
        log.log(Level.INFO, String.format("resolveError - id [%d]", messageId));
        downloadMessageRepository.findById(messageId).ifPresent(message -> downloadMessageHelper.updateStatus(message, Status.RESOLVED));
        return ResponseEntity.ok().build();
    }
}
