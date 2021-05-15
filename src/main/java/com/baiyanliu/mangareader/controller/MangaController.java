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
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
class MangaController {
    private final MangaRepository mangaRepository;
    private final ChapterRepository chapterRepository;
    private final SimpMessagingTemplate webSocket;
    private final DownloaderDispatcher downloaderDispatcher;
    private final TaskManager taskManager;

    @Bean
    public RepresentationModelProcessor<EntityModel<Manga>> mangaProcessor() {
        return new RepresentationModelProcessor<>() {
            @NonNull
            @Override
            public EntityModel<Manga> process(@NonNull EntityModel<Manga> model) {
                Manga manga = model.getContent();
                if (manga != null) {
                    model.add(linkTo(methodOn(MangaController.class).getAllChapters(manga.getId())).withRel("chapters"));
                }
                return model;
            }
        };
    }

    @GetMapping("/chapters/{manga}")
    public ResponseEntity<Map<String, Chapter>> getAllChapters(@PathVariable("manga") Long mangaId) {
        log.log(Level.INFO, String.format("getAllChapters - manga [%d]", mangaId));
        Optional<Manga> manga = mangaRepository.findById(mangaId);
        return manga
                .map(value -> ResponseEntity.ok(value.getChapters()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/page")
    public ResponseEntity<Page> getOnePage(
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
                    return ResponseEntity.ok(chapter.getPages().get(pageNumber));
                }
            }
        }
        return ResponseEntity.notFound().build();
    }

    @RequestMapping("/downloadChapter")
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

    @RequestMapping("/cancelDownload")
    public void cancelDownload(@RequestParam("id") Long messageId) {
        log.log(Level.INFO, String.format("cancelDownload - id [%d]", messageId));
        taskManager.cancelTask(messageId);
    }
}
