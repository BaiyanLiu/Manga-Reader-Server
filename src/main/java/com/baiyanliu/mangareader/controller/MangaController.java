package com.baiyanliu.mangareader.controller;

import com.baiyanliu.mangareader.downloader.DownloaderDispatcher;
import com.baiyanliu.mangareader.entity.Chapter;
import com.baiyanliu.mangareader.entity.Manga;
import com.baiyanliu.mangareader.entity.Page;
import com.baiyanliu.mangareader.entity.repository.MangaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
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
public class MangaController {
    private final MangaRepository mangaRepository;
    private final DownloaderDispatcher downloaderDispatcher;

    @RequestMapping("/api/chapters")
    public Map<Integer, Chapter> getAllChapters(@RequestParam("manga") Long mangaId) {
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
            @RequestParam("chapter") int chapterNumber,
            @RequestParam("page") int pageNumber) {
        log.log(Level.INFO, String.format("getOnePage - manga [%d] chapter [%d] page [%d]", mangaId, chapterNumber, pageNumber));
        Map<Integer, Chapter> chapters = getAllChapters(mangaId);
        if (chapters.containsKey(chapterNumber)) {
            return chapters.get(chapterNumber).getPages().get(pageNumber);
        }
        return null;
    }

    @RequestMapping("/api/downloadChapter")
    public void downloadChapter(
            @RequestParam("manga") Long mangaId,
            @RequestParam("chapter") int chapterNumber) {
        log.log(Level.INFO, String.format("downloadChapter - manga [%d] chapter [%d]", mangaId, chapterNumber));
        Optional<Manga> manga = mangaRepository.findById(mangaId);
        manga.ifPresent(value -> downloaderDispatcher.downloadChapter(value, chapterNumber));
    }
}
