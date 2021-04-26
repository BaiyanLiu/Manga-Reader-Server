package com.baiyanliu.mangareader.downloader;

import com.baiyanliu.mangareader.entity.Chapter;
import com.baiyanliu.mangareader.entity.Manga;
import com.baiyanliu.mangareader.entity.Source;
import com.baiyanliu.mangareader.entity.repository.ChapterRepository;
import com.baiyanliu.mangareader.entity.repository.MangaRepository;
import com.baiyanliu.mangareader.messaging.DownloadChapterMessage;
import com.baiyanliu.mangareader.messaging.DownloadMetadataMessage;
import com.baiyanliu.mangareader.messaging.MessageStatus;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DownloaderDispatcher {
    private final Map<Source, Downloader> downloaders;

    private final MangaRepository mangaRepository;
    private final ChapterRepository chapterRepository;
    private final SimpMessagingTemplate webSocket;

    @Autowired
    public DownloaderDispatcher(MangaRepository mangaRepository, ChapterRepository chapterRepository, SimpMessagingTemplate webSocket) {
        this.mangaRepository = mangaRepository;
        this.chapterRepository = chapterRepository;
        this.webSocket = webSocket;
        downloaders  = ImmutableMap.of(
                Source.MANGA_SEE, new MangaSeeDownloader(webSocket)
        );
    }

    public void downloadMetadata(Manga manga) {
        new DownloadMetadataMessage(manga, MessageStatus.STARTED).send(webSocket);
        downloaders.get(manga.getSource()).downloadMetadata(manga, this::onMetadataDownloaded);
    }

    private void onMetadataDownloaded(Manga manga) {
        mangaRepository.save(manga);
        new DownloadMetadataMessage(manga, MessageStatus.ENDED).send(webSocket);
    }

    public void downloadChapter(Manga manga, String chapterNumber) {
        new DownloadChapterMessage(manga, MessageStatus.STARTED, chapterNumber).send(webSocket);
        downloaders.get(manga.getSource()).downloadChapter(manga, chapterNumber, this::onChapterDownloaded);
    }

    private void onChapterDownloaded(Manga manga, Chapter chapter) {
        chapterRepository.save(chapter);
        new DownloadChapterMessage(manga, MessageStatus.ENDED, chapter.getNumber()).send(webSocket);
    }
}
