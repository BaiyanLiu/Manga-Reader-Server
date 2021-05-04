package com.baiyanliu.mangareader.downloader;

import com.baiyanliu.mangareader.downloader.messaging.DownloadMessage;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class TaskManager {
    private final Map<Long, Future<Void>> tasks = new HashMap<>();

    public synchronized void addTask(DownloadMessage message, Future<Void> task) {
        tasks.put(message.getId(), task);
    }

    public synchronized void removeTask(DownloadMessage message) {
        tasks.remove(message.getId());
    }

    public synchronized void cancelTask(Long messageId) {
        if (tasks.containsKey(messageId)) {
            tasks.get(messageId).cancel(true);
        }
    }
}
