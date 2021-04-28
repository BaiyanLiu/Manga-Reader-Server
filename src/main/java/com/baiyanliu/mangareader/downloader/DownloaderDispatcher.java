package com.baiyanliu.mangareader.downloader;

import com.baiyanliu.mangareader.downloader.messaging.DownloadMessageFactory;
import com.baiyanliu.mangareader.downloader.messaging.MessageStatus;
import com.baiyanliu.mangareader.entity.Chapter;
import com.baiyanliu.mangareader.entity.Manga;
import com.baiyanliu.mangareader.entity.Source;
import com.baiyanliu.mangareader.entity.repository.ChapterRepository;
import com.baiyanliu.mangareader.entity.repository.MangaRepository;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DownloaderDispatcher {
    private final Map<Source, Downloader> downloaders;

    private final MangaRepository mangaRepository;
    private final ChapterRepository chapterRepository;
    private final DownloadMessageFactory downloadMessageFactory;

    @Autowired
    public DownloaderDispatcher(MangaRepository mangaRepository, ChapterRepository chapterRepository, DownloadMessageFactory downloadMessageFactory) {
        this.mangaRepository = mangaRepository;
        this.chapterRepository = chapterRepository;
        this.downloadMessageFactory = downloadMessageFactory;

        downloaders = ImmutableMap.of(
                Source.MANGA_SEE, new MangaSeeDownloader(downloadMessageFactory)
        );
    }

    public void downloadMetadata(Manga manga) {
        downloadMessageFactory.createDownloadMetadataMessage(manga, MessageStatus.START);
        downloaders.get(manga.getSource()).downloadMetadata(manga, this::onMetadataDownloaded);
    }

    private void onMetadataDownloaded(Manga manga) {
        mangaRepository.save(manga);
        downloadMessageFactory.createDownloadMetadataMessage(manga, MessageStatus.END);
    }

    public void downloadChapter(Manga manga, String chapterNumber) {
        downloadMessageFactory.createDownloadChapterMessage(manga, MessageStatus.START, chapterNumber);
        downloaders.get(manga.getSource()).downloadChapter(manga, chapterNumber, this::onChapterDownloaded);
    }

    private void onChapterDownloaded(Manga manga, Chapter chapter) {
        chapterRepository.save(chapter);
        downloadMessageFactory.createDownloadChapterMessage(manga, MessageStatus.END, chapter.getNumber());
    }
}
