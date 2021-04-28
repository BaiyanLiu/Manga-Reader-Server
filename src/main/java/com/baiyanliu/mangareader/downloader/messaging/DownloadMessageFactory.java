package com.baiyanliu.mangareader.downloader.messaging;

import com.baiyanliu.mangareader.entity.Manga;
import com.baiyanliu.mangareader.messaging.ErrorMessageRepository;
import com.baiyanliu.mangareader.messaging.MessageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class DownloadMessageFactory extends MessageFactory {
    private final DownloadMessageRepository downloadMessageRepository;

    @Autowired
    public DownloadMessageFactory(DownloadMessageRepository downloadMessageRepository, ErrorMessageRepository errorMessageRepository, SimpMessagingTemplate webSocket) {
        super(webSocket, errorMessageRepository);
        this.downloadMessageRepository = downloadMessageRepository;
    }

    public void createDownloadMetadataMessage(Manga manga, MessageStatus status) {
        DownloadMessage message = new DownloadMetadataMessage(manga.getName(), status);
        saveAndSend(message);
    }

    public void createDownloadChapterMessage(Manga manga, MessageStatus status, String chapterNumber) {
        DownloadMessage message = new DownloadChapterMessage(manga.getName(), status, chapterNumber);
        saveAndSend(message);
    }

    public void createDownloadPageMessage(Manga manga, MessageStatus status, String chapterNumber, int pageNumber) {
        DownloadMessage message = new DownloadPageMessage(manga.getName(), status, chapterNumber, pageNumber);
        saveAndSend(message);
    }

    private void saveAndSend(DownloadMessage message) {
        downloadMessageRepository.save(message);
        message.send(webSocket);
    }
}
