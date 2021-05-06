package com.baiyanliu.mangareader.downloader;

import com.baiyanliu.mangareader.CustomLogger;
import com.baiyanliu.mangareader.downloader.messaging.DownloadMessage;
import com.baiyanliu.mangareader.downloader.messaging.DownloadMessageHelper;
import com.baiyanliu.mangareader.entity.Chapter;
import com.baiyanliu.mangareader.entity.Manga;
import com.baiyanliu.mangareader.entity.Page;
import lombok.extern.java.Log;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.logging.Level;

@Log
abstract class Downloader {
    private static final int WEB_DRIVER_TIMEOUT = 30;
    private static final int PAGE_DOWNLOAD_DELAY = 2000;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final DownloadMessageHelper downloadMessageHelper;
    private final TaskManager taskManager;
    private final ChromeDriverService chromeDriverService;
    private final ChromeOptions chromeOptions;

    protected Downloader(DownloadMessageHelper downloadMessageHelper, TaskManager taskManager) {
        this.downloadMessageHelper = downloadMessageHelper;
        this.taskManager = taskManager;
        chromeDriverService = new ChromeDriverService.Builder().withSilent(true).build();
        chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless", "--disable-gpu", "--ignore-certificate-errors");
    }

    public void downloadMetadata(Manga manga, Consumer<Manga> callback) {
        CustomLogger logger = new CustomLogger(log, String.format("downloadMetadata - manga [%d] name [%s] source [%s] source ID [%s] ",
                manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId()));
        logger.log(Level.INFO, "Queuing download task", "");
        DownloadMessage message = downloadMessageHelper.createDownloadMetadataMessage(manga);

        Future<Void> task = executor.submit(() -> {
            WebDriver driver = null;
            try {
                String url = getMetadataUrl(manga);
                logger.log(Level.INFO, "Starting download task", String.format("URL [%s] ", url));

                driver = new ChromeDriver(chromeDriverService, chromeOptions);
                driver.get(url);

                if (isLoadChaptersRequired()) {
                    try {
                        new WebDriverWait(driver, WEB_DRIVER_TIMEOUT)
                                .ignoring(ElementNotInteractableException.class, StaleElementReferenceException.class)
                                .until((WebDriver d) -> {
                                    logger.log(Level.INFO, "Waiting for elements to load", String.format("URL [%s] ", url));
                                    loadChapters(d);
                                    logger.log(Level.INFO, "Elements loaded", String.format("URL [%s] ", url));
                                    return true;
                                });
                    } catch (WebDriverException e) {
                        throw (e.getCause() instanceof InterruptedException ? (InterruptedException) e.getCause() : e);
                    }
                }

                for (String chapterNumber : getChapterNumbers(driver)) {
                    logger.log(Level.INFO, "Found chapter", String.format("URL [%s] chapter [%s] ", url, chapterNumber));
                    if (!manga.getChapters().containsKey(chapterNumber)) {
                        Chapter chapter = new Chapter(chapterNumber, "Chapter " + chapterNumber);
                        manga.getChapters().put(chapter.getNumber(), chapter);
                    }
                }

                logger.log(Level.INFO, "Finished download task", String.format("URL [%s] chapters [%d] ", url, manga.getChapters().size()));
                downloadMessageHelper.updateCompleted(message, 1);

                callback.accept(manga);
            } catch (InterruptedException e) {
                logger.log(Level.INFO, "Cancelling download task", "");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error encountered", "", e);
                downloadMessageHelper.createErrorMessage(e.getLocalizedMessage());
            } finally {
                taskManager.removeTask(message);
                if (driver != null) {
                    driver.quit();
                }
            }
            return null;
        });
        taskManager.addTask(message, task);
    }

    protected abstract String getMetadataUrl(Manga manga);

    protected boolean isLoadChaptersRequired() {
        return false;
    }

    protected void loadChapters(WebDriver driver) {}

    protected abstract List<String> getChapterNumbers(WebDriver driver);

    public void downloadChapter(Manga manga, String chapterNumber, Consumer<Chapter> callback) {
        CustomLogger logger = new CustomLogger(log, String.format("downloadChapter - manga [%d] name [%s] source [%s] source ID [%s] chapter [%s] ",
                manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId(), chapterNumber));
        logger.log(Level.INFO, "Queuing download task", "");
        DownloadMessage message = downloadMessageHelper.createDownloadChapterMessage(manga, chapterNumber);

        Future<Void> task = executor.submit(() -> {
            WebDriver driver = null;
            try {
                logger.log(Level.INFO, "Starting download task", "");

                driver = new ChromeDriver(chromeDriverService, chromeOptions);
                Chapter chapter = manga.getChapters().get(chapterNumber);
                chapter.getPages().clear();
                int pageNumber = 1;

                while (true) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }

                    String url = getPageUrl(manga, chapterNumber, pageNumber);
                    logger.log(Level.INFO, "Downloading page", String.format("page [%d] pages [%d] URL [%s] ", pageNumber, chapter.getLastPage(), url));
                    driver.get(url);

                    Page page = new Page(pageNumber);

                    try {
                        new WebDriverWait(driver, WEB_DRIVER_TIMEOUT)
                                .ignoring(ElementNotInteractableException.class, StaleElementReferenceException.class)
                                .until((WebDriver d) -> {
                                    logger.log(Level.INFO, "Waiting for elements to load", String.format("page [%d] pages [%d] URL [%s] ", page.getNumber(), chapter.getLastPage(), url));

                                    if (page.getNumber() == 1) {
                                        int lastPage = getLastPage(d);
                                        chapter.setLastPage(lastPage);
                                        message.setTotal(lastPage);
                                    }

                                    String src = getImageSource(d);
                                    if (src == null) {
                                        return false;
                                    }

                                    logger.log(Level.INFO, "Elements loaded", String.format("page [%d] pages [%d] URL [%s] ", page.getNumber(), chapter.getLastPage(), url));

                                    try {
                                        BufferedImage image = ImageIO.read(new URL(src));
                                        page.setImage(image);
                                        chapter.getPages().put(page.getNumber(), page);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return true;
                                });
                    } catch (WebDriverException e) {
                        throw (e.getCause() instanceof InterruptedException ? (InterruptedException) e.getCause() : e);
                    }

                    logger.log(Level.INFO, "Finished downloading page", String.format("page [%d] pages [%d] URL [%s] ", pageNumber, chapter.getLastPage(), url));
                    downloadMessageHelper.updateCompleted(message, pageNumber);

                    if (pageNumber == chapter.getLastPage()) {
                        break;
                    }
                    pageNumber++;

                    Thread.sleep(PAGE_DOWNLOAD_DELAY);
                }

                chapter.setDownloaded(true);
                logger.log(Level.INFO, "Finished download task", String.format("pages [%d] ", chapter.getLastPage()));

                callback.accept(chapter);
            } catch (InterruptedException e) {
                logger.log(Level.INFO, "Cancelling download task", "");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error encountered", "", e);
                downloadMessageHelper.createErrorMessage(e.getLocalizedMessage());
            } finally {
                taskManager.removeTask(message);
                if (driver != null) {
                    driver.quit();
                }
            }
            return null;
        });
        taskManager.addTask(message, task);
    }

    protected abstract String getPageUrl(Manga manga, String chapterNumber, int pageNumber);

    protected abstract int getLastPage(WebDriver driver);

    protected abstract String getImageSource(WebDriver driver);
}
