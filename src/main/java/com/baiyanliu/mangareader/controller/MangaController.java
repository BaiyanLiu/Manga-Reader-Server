package com.baiyanliu.mangareader.controller;

import com.baiyanliu.mangareader.downloader.DownloaderDispatcher;
import com.baiyanliu.mangareader.downloader.TaskManager;
import com.baiyanliu.mangareader.downloader.messaging.DownloadMessage;
import com.baiyanliu.mangareader.downloader.messaging.DownloadMessageHelper;
import com.baiyanliu.mangareader.downloader.messaging.DownloadMessageRepository;
import com.baiyanliu.mangareader.downloader.messaging.Status;
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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.logging.Level;

@Log
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
class MangaController {
    private final MangaRepository mangaRepository;
    private final ChapterRepository chapterRepository;
    private final DownloadMessageRepository downloadMessageRepository;
    private final SimpMessagingTemplate webSocket;
    private final DownloaderDispatcher downloaderDispatcher;
    private final TaskManager taskManager;
    private final DownloadMessageHelper downloadMessageHelper;

    @GetMapping("/chapters/{manga}")
    public ResponseEntity<CollectionModel<EntityModel<Chapter>>> getAllChapters(@PathVariable("manga") Long mangaId) {
        log.log(Level.INFO, String.format("getAllChapters - manga [%d]", mangaId));
        Optional<Manga> manga = mangaRepository.findById(mangaId);
        if (manga.isPresent()) {
            List<EntityModel<Chapter>> chapters = new ArrayList<>();
            for (Chapter chapter : manga.get().getChapters().values()) {
                chapters.add(EntityModel.of(chapter));
            }
            return ResponseEntity.ok(CollectionModel.of(chapters));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/page")
    public ResponseEntity<EntityModel<Page>> getOnePage(
            @RequestParam("manga") Long mangaId,
            @RequestParam("chapter") String chapterNumber,
            @RequestParam("page") int pageNumber) {
        log.log(Level.INFO, String.format("getOnePage - manga [%d] chapter [%s] page [%d]", mangaId, chapterNumber, pageNumber));
        Optional<Manga> manga = mangaRepository.findById(mangaId);
        if (manga.isPresent()) {
            Map<String, Chapter> chapters = manga.get().getChapters();
            if (chapters.containsKey(chapterNumber)) {
                Chapter chapter = chapters.get(chapterNumber);
                if (chapter.getPages().containsKey(pageNumber)) {
                    if (pageNumber == chapter.getLastPage()) {
                        chapter.setRead(true);
                        chapterRepository.save(chapter);
                        new ChapterUpdateMessage(mangaId, Collections.singletonList(chapter)).send(webSocket);
                    }
                    return ResponseEntity.ok(EntityModel.of(chapter.getPages().get(pageNumber)));
                }
            }
        }
        return ResponseEntity.notFound().build();
    }

    @RequestMapping("/downloadChapter")
    public ResponseEntity<Void> downloadChapter(
            @RequestParam("manga") Long mangaId,
            @RequestParam("chapter") String chapterNumber) {
        log.log(Level.INFO, String.format("downloadChapter - manga [%d] chapter [%s]", mangaId, chapterNumber));
        Optional<Manga> manga = mangaRepository.findById(mangaId);
        manga.ifPresent(value -> {
            Hibernate.initialize(value.getChapters());
            Hibernate.initialize(value.getChapters().get(chapterNumber).getPages());
            downloaderDispatcher.downloadChapter(value, chapterNumber);
        });
        return ResponseEntity.ok().build();
    }

    @RequestMapping("/cancelDownload/{id}")
    public ResponseEntity<Void> cancelDownload(@PathVariable("id") Long messageId) {
        log.log(Level.INFO, String.format("cancelDownload - id [%d]", messageId));
        taskManager.cancelTask(messageId);
        return ResponseEntity.ok().build();
    }

    @RequestMapping("/resolveError/{id}")
    public ResponseEntity<Void> resolveError(@PathVariable("id") Long messageId) {
        log.log(Level.INFO, String.format("resolveError - id [%d]", messageId));
        Optional<DownloadMessage> message = downloadMessageRepository.findById(messageId);
        message.ifPresent(value -> downloadMessageHelper.updateStatus(value, Status.RESOLVED));
        return ResponseEntity.ok().build();
    }
}
