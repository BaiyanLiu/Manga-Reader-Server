package com.baiyanliu.mangareader.entity.repository;

import com.baiyanliu.mangareader.downloader.DownloaderDispatcher;
import com.baiyanliu.mangareader.entity.Manga;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.util.logging.Level;

@Component
@Log
@RepositoryEventHandler
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
class EventHandler {
    private final DownloaderDispatcher downloaderDispatcher;

    @HandleAfterCreate
    public void onCreateManga(Manga manga) {
        log.log(Level.INFO, String.format("onCreateManga - manga [%d] name [%s]", manga.getId(), manga.getName()));
        downloaderDispatcher.downloadMetadata(manga);
    }
}
