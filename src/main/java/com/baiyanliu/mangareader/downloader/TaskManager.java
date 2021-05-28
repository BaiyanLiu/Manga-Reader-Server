package com.baiyanliu.mangareader.downloader;

import com.baiyanliu.mangareader.downloader.messaging.DownloadMessage;
import com.baiyanliu.mangareader.downloader.messaging.DownloadMessageHelper;
import com.baiyanliu.mangareader.downloader.messaging.Status;
import com.baiyanliu.mangareader.downloader.messaging.repository.DownloadMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Service
public class TaskManager {
    private final Map<Long, Future<Void>> tasks = new HashMap<>();

    private final DownloadMessageRepository downloadMessageRepository;
    private final DownloadMessageHelper downloadMessageHelper;

    public synchronized void addTask(DownloadMessage message, Future<Void> task) {
        tasks.put(message.getId(), task);
    }

    public synchronized void removeTask(DownloadMessage message) {
        tasks.remove(message.getId());
    }

    public synchronized void cancelTask(Long messageId) {
        if (tasks.containsKey(messageId)) {
            tasks.get(messageId).cancel(true);
            Optional<DownloadMessage> message = downloadMessageRepository.findById(messageId);
            message.ifPresent(downloadMessage -> downloadMessageHelper.updateStatus(downloadMessage, Status.CANCELLED));
        }
    }
}
