package com.baiyanliu.mangareader.downloader.messaging;

import com.baiyanliu.mangareader.entity.Manga;
import com.baiyanliu.mangareader.messaging.ErrorMessageRepository;
import com.baiyanliu.mangareader.messaging.MessageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DownloadMessageHelper extends MessageFactory {
    private final DownloadMessageRepository downloadMessageRepository;

    @Autowired
    public DownloadMessageHelper(DownloadMessageRepository downloadMessageRepository, ErrorMessageRepository errorMessageRepository, SimpMessagingTemplate webSocket) {
        super(webSocket, errorMessageRepository);
        this.downloadMessageRepository = downloadMessageRepository;
    }

    public DownloadMessage createDownloadMetadataMessage(Manga manga) {
        DownloadMessage message = new DownloadMetadataMessage(manga.getName(), MessageStatus.START);
        saveAndSend(message);
        return message;
    }

    public DownloadMessage createDownloadChapterMessage(Manga manga, String chapterNumber) {
        DownloadMessage message = new DownloadChapterMessage(manga.getName(), MessageStatus.START, chapterNumber);
        saveAndSend(message);
        return message;
    }

    public DownloadMessage createDownloadPageMessage(Manga manga, String chapterNumber, int pageNumber) {
        DownloadMessage message = new DownloadPageMessage(manga.getName(), MessageStatus.START, chapterNumber, pageNumber);
        saveAndSend(message);
        return message;
    }

    public void updateStatus(DownloadMessage message, MessageStatus status) {
        message.setStatus(status);
        message.setTimestamp(new Date());
        saveAndSend(message);
    }

    private void saveAndSend(DownloadMessage message) {
        downloadMessageRepository.save(message);
        message.send(webSocket);
    }
}
