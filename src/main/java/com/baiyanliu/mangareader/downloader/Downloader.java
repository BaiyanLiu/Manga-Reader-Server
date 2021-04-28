package com.baiyanliu.mangareader.downloader;

import com.baiyanliu.mangareader.downloader.messaging.DownloadMessageFactory;
import com.baiyanliu.mangareader.entity.Chapter;
import com.baiyanliu.mangareader.entity.Manga;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

abstract class Downloader {
    protected static final int WEB_DRIVER_TIMEOUT = 30;
    protected static final int PAGE_DOWNLOAD_DELAY = 2000;

    protected final ExecutorService executor = Executors.newSingleThreadExecutor();

    protected final DownloadMessageFactory downloadMessageFactory;
    protected final ChromeOptions chromeOptions;

    protected Downloader(DownloadMessageFactory downloadMessageFactory) {
        this.downloadMessageFactory = downloadMessageFactory;
        chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless", "--disable-gpu", "--ignore-certificate-errors");
    }

    public abstract void downloadMetadata(Manga manga, Consumer<Manga> callback);

    public abstract void downloadChapter(Manga manga, String chapterNumber, BiConsumer<Manga, Chapter> callback);
}
