package com.baiyanliu.mangareader.downloader;

import com.baiyanliu.mangareader.controller.MangaController;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class DownloaderScheduler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final MangaController mangaController;

    @PostConstruct
    public void schedule() {
        scheduler.scheduleAtFixedRate(mangaController::updateAll, 1, 8, TimeUnit.HOURS);
        scheduler.scheduleAtFixedRate(mangaController::downloadAll, 2, 8, TimeUnit.HOURS);
    }
}
