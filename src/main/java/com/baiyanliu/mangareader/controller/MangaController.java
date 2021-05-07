package com.baiyanliu.mangareader.controller;

import com.baiyanliu.mangareader.downloader.DownloaderDispatcher;
import com.baiyanliu.mangareader.downloader.TaskManager;
import com.baiyanliu.mangareader.entity.Chapter;
import com.baiyanliu.mangareader.entity.Manga;
import com.baiyanliu.mangareader.entity.Page;
import com.baiyanliu.mangareader.entity.repository.ChapterRepository;
import com.baiyanliu.mangareader.entity.repository.MangaRepository;
import com.baiyanliu.mangareader.messaging.ChapterUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

@Log
@RestController
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
class MangaController {
    private final MangaRepository mangaRepository;
    private final ChapterRepository chapterRepository;
    private final SimpMessagingTemplate webSocket;
    private final DownloaderDispatcher downloaderDispatcher;
    private final TaskManager taskManager;

    @RequestMapping("/api/chapters")
    public Map<String, Chapter> getAllChapters(@RequestParam("manga") Long mangaId) {
        log.log(Level.INFO, String.format("getAllChapters - manga [%d]", mangaId));
        Optional<Manga> manga = mangaRepository.findById(mangaId);
        if (manga.isPresent()) {
            return manga.get().getChapters();
        }
        return Collections.emptyMap();
    }

    @RequestMapping("/api/page")
    public Page getOnePage(
            @RequestParam("manga") Long mangaId,
            @RequestParam("chapter") String chapterNumber,
            @RequestParam("page") int pageNumber) {
        log.log(Level.INFO, String.format("getOnePage - manga [%d] chapter [%s] page [%d]", mangaId, chapterNumber, pageNumber));
        Map<String, Chapter> chapters = getAllChapters(mangaId);
        if (chapters.containsKey(chapterNumber)) {
            Chapter chapter = chapters.get(chapterNumber);
            if (chapter.getPages().containsKey(pageNumber)) {
                if (pageNumber == chapter.getLastPage()) {
                    chapter.setRead(true);
                    chapterRepository.save(chapter);
                    new ChapterUpdateMessage(mangaId, Collections.singletonList(chapter)).send(webSocket);
                }
                return chapter.getPages().get(pageNumber);
            }
        }
        return null;
    }

    @RequestMapping("/api/downloadChapter")
    public void downloadChapter(
            @RequestParam("manga") Long mangaId,
            @RequestParam("chapter") String chapterNumber) {
        log.log(Level.INFO, String.format("downloadChapter - manga [%d] chapter [%s]", mangaId, chapterNumber));
        Optional<Manga> manga = mangaRepository.findById(mangaId);
        manga.ifPresent(value -> {
            Hibernate.initialize(value.getChapters());
            Hibernate.initialize(value.getChapters().get(chapterNumber).getPages());
            downloaderDispatcher.downloadChapter(value, chapterNumber);
        });
    }

    @RequestMapping("/api/cancelDownload")
    public void cancelDownload(@RequestParam("id") Long messageId) {
        log.log(Level.INFO, String.format("cancelDownload - id [%d]", messageId));
        taskManager.cancelTask(messageId);
    }
}
