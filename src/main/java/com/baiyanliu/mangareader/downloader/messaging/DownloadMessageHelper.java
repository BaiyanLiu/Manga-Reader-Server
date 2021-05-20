package com.baiyanliu.mangareader.downloader.messaging;

import com.baiyanliu.mangareader.entity.Manga;
import com.baiyanliu.mangareader.messaging.ErrorMessageRepository;
import com.baiyanliu.mangareader.messaging.MessageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class DownloadMessageHelper extends MessageFactory {
    private final DownloadMessageRepository downloadMessageRepository;

    @Autowired
    public DownloadMessageHelper(DownloadMessageRepository downloadMessageRepository, ErrorMessageRepository errorMessageRepository, SimpMessagingTemplate webSocket) {
        super(webSocket, errorMessageRepository);
        this.downloadMessageRepository = downloadMessageRepository;
    }

    public DownloadMessage createDownloadMetadataMessage(Manga manga) {
        DownloadMessage message = new DownloadMetadataMessage(manga);
        saveAndSend(message);
        return message;
    }

    public DownloadMessage createDownloadChapterMessage(Manga manga, String chapterNumber) {
        DownloadMessage message = new DownloadChapterMessage(manga, chapterNumber);
        saveAndSend(message);
        return message;
    }

    public void updateCompleted(DownloadMessage message, int completed) {
        message.setCompleted(completed);
        if (completed == message.getTotal()) {
            message.setStatus(Status.COMPLETED);
        }
        saveAndSend(message);
    }

    public void updateStatus(DownloadMessage message, Status status) {
        message.setStatus(status);
        saveAndSend(message);
    }

    private void saveAndSend(DownloadMessage message) {
        downloadMessageRepository.save(message);
        message.send(webSocket);
    }
}
