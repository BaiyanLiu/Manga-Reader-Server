package com.baiyanliu.mangareader.messaging;

import com.baiyanliu.mangareader.entity.Chapter;
import com.baiyanliu.mangareader.entity.Manga;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class MessageFactory {
    protected final SimpMessagingTemplate webSocket;

    private final ErrorMessageRepository errorMessageRepository;

    public void createErrorMessage(String error) {
        ErrorMessage message = new ErrorMessage(error);
        errorMessageRepository.save(message);
        message.send(webSocket);
    }

    public void createMangaMessage(Manga manga) {
        MangaMessage message = new MangaMessage(manga);
        message.send(webSocket);
    }

    public void createChapterMessage(Manga manga, Collection<Chapter> chapters) {
        ChapterMessage message = new ChapterMessage(manga, chapters);
        message.send(webSocket);
    }
}
