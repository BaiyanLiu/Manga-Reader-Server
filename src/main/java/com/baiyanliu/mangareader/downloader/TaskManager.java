package com.baiyanliu.mangareader.downloader;

import com.baiyanliu.mangareader.downloader.messaging.DownloadMessage;
import com.baiyanliu.mangareader.downloader.messaging.DownloadMessageHelper;
import com.baiyanliu.mangareader.downloader.messaging.Status;
import com.baiyanliu.mangareader.downloader.messaging.repository.DownloadMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

@Component
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
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
        Future<Void> task = tasks.remove(messageId);
        if (task != null) {
            task.cancel(true);
            Optional<DownloadMessage> message = downloadMessageRepository.findById(messageId);
            message.ifPresent(downloadMessage -> downloadMessageHelper.updateStatus(downloadMessage, Status.CANCELLED));
        }
    }

    public synchronized void cancelAll() {
        tasks.values().forEach((task) -> task.cancel(true));
        downloadMessageHelper.updateStatusAll(downloadMessageRepository.findAllById(tasks.keySet()), Status.CANCELLED);
        tasks.clear();
    }
}
