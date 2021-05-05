package com.baiyanliu.mangareader.downloader;

import com.baiyanliu.mangareader.downloader.messaging.DownloadMessageHelper;
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

    @Autowired
    public DownloaderDispatcher(MangaRepository mangaRepository, ChapterRepository chapterRepository, DownloadMessageHelper downloadMessageHelper, TaskManager taskManager) {
        this.mangaRepository = mangaRepository;
        this.chapterRepository = chapterRepository;
        downloaders = ImmutableMap.of(
                Source.MANGA_SEE, new MangaSeeDownloader(downloadMessageHelper, taskManager)
        );
    }

    public void downloadMetadata(Manga manga) {
        downloaders.get(manga.getSource()).downloadMetadata(manga, this::onMetadataDownloaded);
    }

    private void onMetadataDownloaded(Manga manga) {
        mangaRepository.save(manga);
    }

    public void downloadChapter(Manga manga, String chapterNumber) {
        downloaders.get(manga.getSource()).downloadChapter(manga, chapterNumber, this::onChapterDownloaded);
    }

    private void onChapterDownloaded(Chapter chapter) {
        chapterRepository.save(chapter);
    }
}
