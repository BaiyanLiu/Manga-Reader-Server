package com.baiyanliu.mangareader.downloader;

import com.baiyanliu.mangareader.controller.MangaController;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ConfigurationProperties(prefix = "downloader.scheduler")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Service
public class DownloaderScheduler {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final MangaController mangaController;

    @Getter @Setter private boolean enabled = true;
    @Getter @Setter private int period = 8;

    @PostConstruct
    public void schedule() {
        if (enabled) {
            scheduler.scheduleAtFixedRate(mangaController::updateAll, 0, period, TimeUnit.HOURS);
            scheduler.scheduleAtFixedRate(mangaController::downloadAll, 1, period, TimeUnit.HOURS);
        }
    }
}
