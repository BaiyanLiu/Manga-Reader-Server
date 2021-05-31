package com.baiyanliu.mangareader.controller;

import com.baiyanliu.mangareader.downloader.DownloaderDispatcher;
import com.baiyanliu.mangareader.downloader.TaskManager;
import com.baiyanliu.mangareader.downloader.messaging.DownloadMessageHelper;
import com.baiyanliu.mangareader.downloader.messaging.Status;
import com.baiyanliu.mangareader.downloader.messaging.repository.DownloadMessageRepository;
import com.baiyanliu.mangareader.entity.Chapter;
import com.baiyanliu.mangareader.entity.Manga;
import com.baiyanliu.mangareader.entity.Page;
import com.baiyanliu.mangareader.entity.repository.ChapterRepository;
import com.baiyanliu.mangareader.entity.repository.MangaRepository;
import com.baiyanliu.mangareader.messaging.MessageFactory;
import com.baiyanliu.mangareader.messaging.UpdateType;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Log
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class MangaController {
    private final MangaRepository mangaRepository;
    private final ChapterRepository chapterRepository;
    private final DownloadMessageRepository downloadMessageRepository;

    private final DownloaderDispatcher downloaderDispatcher;
    private final TaskManager taskManager;
    private final DownloadMessageHelper downloadMessageHelper;
    private final MessageFactory messageFactory;

    @GetMapping("/chapters/{manga}")
    public ResponseEntity<CollectionModel<EntityModel<Chapter>>> getChapters(@PathVariable("manga") Long mangaId) {
        log.log(Level.INFO, String.format("getChapters - manga [%d]", mangaId));
        Optional<Manga> manga = mangaRepository.findById(mangaId);
        if (manga.isPresent()) {
            List<EntityModel<Chapter>> chapters = manga.get().getChapters().values().stream().map(EntityModel::of).collect(Collectors.toList());
            return ResponseEntity.ok(CollectionModel.of(chapters));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/page")
    @Transactional
    public ResponseEntity<EntityModel<Page>> getPage(
            @RequestParam("manga") Long mangaId,
            @RequestParam("chapter") String chapterNumber,
            @RequestParam("page") int pageNumber) {
        log.log(Level.INFO, String.format("getPage - manga [%d] chapter [%s] page [%d]", mangaId, chapterNumber, pageNumber));
        Optional<Manga> mangaOptional = mangaRepository.findById(mangaId);
        if (mangaOptional.isPresent()) {
            Manga manga = mangaOptional.get();
            Chapter chapter = manga.getChapters().get(chapterNumber);
            if (chapter != null) {
                Page page = chapter.getPages().get(pageNumber);
                if (page != null) {

                    if (pageNumber == chapter.getLastPage() && !chapter.isRead()) {
                        manga.setLastRead(new Date());
                        if (!chapter.isIgnored()) {
                            manga.updateUnread(-1);
                        }
                        chapter.setRead(true);

                        mangaRepository.save(manga);
                        chapterRepository.save(chapter);
                        messageFactory.createMangaMessage(manga);
                        messageFactory.createChapterMessage(manga, Collections.singletonList(chapter), UpdateType.UPDATE);
                    }

                    return ResponseEntity.ok(EntityModel.of(page));
                }
            }
        }
        return ResponseEntity.notFound().build();
    }

    @RequestMapping("/updateManga/{manga}")
    public ResponseEntity<Void> updateManga(@PathVariable("manga") Long mangaId) {
        log.log(Level.INFO, String.format("updateManga - manga [%d]", mangaId));
        mangaRepository.findById(mangaId).ifPresent(this::updateManga);
        return ResponseEntity.ok().build();
    }

    @RequestMapping("/updateAll")
    @Transactional
    public ResponseEntity<Void> updateAll() {
        log.log(Level.INFO, "updateAll");
        mangaRepository.findAll().forEach(this::updateManga);
        return ResponseEntity.ok().build();
    }

    private void updateManga(Manga manga) {
        Hibernate.initialize(manga.getChapters());
        downloaderDispatcher.downloadMetadata(manga);
    }

    @RequestMapping("/downloadManga/{manga}")
    public ResponseEntity<Void> downloadManga(@PathVariable("manga") Long mangaId) {
        log.log(Level.INFO, String.format("downloadManga - manga [%d]", mangaId));
        mangaRepository.findById(mangaId).ifPresent(this::downloadManga);
        return ResponseEntity.ok().build();
    }

    @RequestMapping("/downloadAll")
    @Transactional
    public ResponseEntity<Void> downloadAll() {
        log.log(Level.INFO, "downloadAll");
        mangaRepository.findAll().forEach(this::downloadManga);
        return ResponseEntity.ok().build();
    }

    private void downloadManga(Manga manga) {
        Hibernate.initialize(manga.getChapters());
        for (Chapter chapter : manga.getChapters().values()) {
            if (!chapter.isIgnored() && !chapter.isDownloaded()) {
                downloadChapter(manga, chapter.getNumber());
            }
        }
    }

    @RequestMapping("/downloadChapter")
    public ResponseEntity<Void> downloadChapter(
            @RequestParam("manga") Long mangaId,
            @RequestParam("chapter") String chapterNumber) {
        log.log(Level.INFO, String.format("downloadChapter - manga [%d] chapter [%s]", mangaId, chapterNumber));
        mangaRepository.findById(mangaId).ifPresent(manga -> {
            Hibernate.initialize(manga.getChapters());
            downloadChapter(manga, chapterNumber);
        });
        return ResponseEntity.ok().build();
    }

    private void downloadChapter(Manga manga, String chapterNumber) {
        Hibernate.initialize(manga.getChapters().get(chapterNumber).getPages());
        downloaderDispatcher.downloadChapter(manga, chapterNumber);
    }

    @RequestMapping("/ignoreChapter")
    @Transactional
    public ResponseEntity<Void> ignoreChapter(
            @RequestParam("manga") Long mangaId,
            @RequestParam("chapter") String chapterNumber) {
        log.log(Level.INFO, String.format("ignoreChapter - manga [%d] chapter [%s]", mangaId, chapterNumber));
        mangaRepository.findById(mangaId).ifPresent(manga -> {
            Chapter chapter = manga.getChapters().get(chapterNumber);
            if (chapter != null) {
                chapter.setIgnored(!chapter.isIgnored());
                boolean updateManga = !chapter.isRead();
                if (updateManga) {
                    manga.updateUnread(chapter.isIgnored() ? -1 : 1);
                    mangaRepository.save(manga);
                }

                chapterRepository.save(chapter);
                if (updateManga) {
                    messageFactory.createMangaMessage(manga);
                }
                messageFactory.createChapterMessage(manga, Collections.singletonList(chapter), UpdateType.UPDATE);
            }
        });
        return ResponseEntity.ok().build();
    }

    @RequestMapping("/deleteChapter")
    public ResponseEntity<Void> deleteChapter(
            @RequestParam("manga") Long mangaId,
            @RequestParam("chapter") String chapterNumber) {
        log.log(Level.INFO, String.format("deleteChapter - manga [%d] chapter [%s]", mangaId, chapterNumber));
        mangaRepository.findById(mangaId).ifPresent(manga -> {
            Chapter chapter = manga.getChapters().remove(chapterNumber);
            if (chapter != null) {
                boolean updateManga = !chapter.isRead() && !chapter.isIgnored();
                if (updateManga) {
                    manga.updateUnread(-1);
                }

                mangaRepository.save(manga);
                if (updateManga) {
                    messageFactory.createMangaMessage(manga);
                }
                messageFactory.createChapterMessage(manga, Collections.singletonList(chapter), UpdateType.DELETE);
            }
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
        downloadMessageRepository.findById(messageId).ifPresent(message -> downloadMessageHelper.updateStatus(message, Status.RESOLVED));
        return ResponseEntity.ok().build();
    }
}
