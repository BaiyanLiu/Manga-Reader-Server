package com.baiyanliu.mangareader.downloader;

import com.baiyanliu.mangareader.entity.Chapter;
import com.baiyanliu.mangareader.entity.Manga;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public abstract class Downloader {
    protected static final int WEB_DRIVER_TIMEOUT = 300;
    protected static final int PAGE_DOWNLOAD_DELAY = 2000;

    protected final ExecutorService executor = Executors.newSingleThreadExecutor();

    protected final WebDriver driver;

    protected Downloader() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--ignore-certificate-errors");
        driver = new ChromeDriver(options);
    }

    public abstract void downloadMetadata(Manga manga, Consumer<Manga> callback);

    public abstract void downloadChapter(Manga manga, String chapterNumber, Consumer<Chapter> callback);
}
