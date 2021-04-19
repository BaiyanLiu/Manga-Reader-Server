package com.baiyanliu.mangareader.downloader;

import com.baiyanliu.mangareader.CustomLogger;
import com.baiyanliu.mangareader.entity.Chapter;
import com.baiyanliu.mangareader.entity.Manga;
import com.baiyanliu.mangareader.entity.Page;
import lombok.extern.java.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;
import java.util.logging.Level;

@Log
public class MangaSeeDownloader extends Downloader {
    private static final String HOME_URL = "https://mangasee123.com/manga/%s";
    private static final String PAGE_URL = "https://mangasee123.com/read-online/%s-chapter-%s-page-%d.html";

    @Override
    public void downloadMetadata(Manga manga, Consumer<Manga> callback) {
        CustomLogger logger = new CustomLogger(log, String.format("downloadMetadata - manga [%d] name [%s] source [%s] source ID [%s] ",
                manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId()));
        logger.log(Level.INFO, "Queuing download task", "");
        executor.submit(() -> {
            try {
                String url = String.format(HOME_URL, manga.getSourceId());
                logger.log(Level.INFO, "Starting download task", String.format("URL [%s] ", url));

                driver.get(url);

                new WebDriverWait(driver, WEB_DRIVER_TIMEOUT)
                        .ignoring(StaleElementReferenceException.class)
                        .until((WebDriver d) -> {
                            logger.log(Level.INFO, "Waiting for elements to load", String.format("URL [%s] ", url));
                            d.findElement(By.className("ShowAllChapters")).click();
                            logger.log(Level.INFO, "Elements loaded", String.format("URL [%s] ", url));
                            return true;
                        });

                for (WebElement c : driver.findElements(By.className("ChapterLink"))) {
                    String chapterNumber = extractChapterNumber(c.getAttribute("href"));
                    logger.log(Level.INFO, "Found chapter", String.format("URL [%s] chapter [%s] ", url, chapterNumber));
                    if (!manga.getChapters().containsKey(chapterNumber)) {
                        Chapter chapter = new Chapter(chapterNumber, "Chapter " + chapterNumber);
                        manga.getChapters().put(chapter.getNumber(), chapter);
                    }
                }

                logger.log(Level.INFO, "Finished download task", String.format("URL [%s] chapters [%d] ", url, manga.getChapters().size()));
                callback.accept(manga);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error encountered", "", e);
            }
        });
    }

    private String extractChapterNumber(String url) {
        int beginIndex = url.indexOf("-chapter-") + 9;
        int endIndex = url.indexOf("-page-");
        return url.substring(beginIndex, endIndex);
    }

    @Override
    public void downloadChapter(Manga manga, String chapterNumber, Consumer<Chapter> callback) {
        CustomLogger logger = new CustomLogger(log, String.format("downloadChapter - manga [%d] name [%s] source [%s] source ID [%s] chapter [%s] ",
                manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId(), chapterNumber));
        logger.log(Level.INFO, "Queuing download task", "");
        executor.submit(() -> {
            try {
                logger.log(Level.INFO, "Starting download task", "");

                Chapter chapter = manga.getChapters().get(chapterNumber);
                int pageNumber = 1;

                while (true) {
                    logger.log(Level.INFO, "Starting to download page", String.format("page [%d] ", pageNumber));

                    if (chapter.getPages().containsKey(pageNumber)) {
                        pageNumber++;
                        continue;
                    }

                    String url = String.format(PAGE_URL, manga.getSourceId(), chapterNumber, pageNumber);
                    logger.log(Level.INFO, "Downloading page", String.format("page [%d] URL [%s] ", pageNumber, url));

                    driver.get(String.format(PAGE_URL, manga.getSourceId(), chapterNumber, pageNumber));
                    if ("404 Page Not Found".equals(driver.getTitle())) {
                        logger.log(Level.INFO, "Reached last page", String.format("page [%d] URL [%s] ", pageNumber, url));
                        break;
                    }

                    Page page = new Page(pageNumber);

                    new WebDriverWait(driver, WEB_DRIVER_TIMEOUT)
                            .ignoring(StaleElementReferenceException.class)
                            .until((WebDriver d) -> {
                                logger.log(Level.INFO, "Waiting for elements to load", String.format("page [%d] URL [%s] ", page.getNumber(), url));
                                String src = d.findElement(By.className("img-fluid")).getAttribute("src");
                                if (src == null) {
                                    return false;
                                }
                                logger.log(Level.INFO, "Elements loaded", String.format("page [%d] URL [%s] ", page.getNumber(), url));

                                try {
                                    BufferedImage image = ImageIO.read(new URL(src));
                                    page.setData(image);
                                    chapter.getPages().put(page.getNumber(), page);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return true;
                            });

                    logger.log(Level.INFO, "Finished downloading page", String.format("page [%d] URL [%s] ", page.getNumber(), url));
                    pageNumber++;
                    try {
                        Thread.sleep(PAGE_DOWNLOAD_DELAY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                chapter.setDownloaded(true);
                logger.log(Level.INFO, "Finished download task", String.format("pages [%d] ", chapter.getPages().size()));
                callback.accept(chapter);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error encountered", "", e);
            }
        });
    }
}
