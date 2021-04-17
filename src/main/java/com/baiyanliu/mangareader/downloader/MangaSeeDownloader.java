package com.baiyanliu.mangareader.downloader;

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
    private static final String PAGE_URL = "https://mangasee123.com/read-online/%s-chapter-%d-page-%d.html";

    @Override
    public void downloadMetadata(Manga manga, Consumer<Manga> callback) {
        log.log(Level.INFO, String.format("downloadMetadata - manga [%d] name [%s] source [%s] source ID [%s] - Queuing download task.",
                manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId()));
        executor.submit(() -> {
            try {
                String url = String.format(HOME_URL, manga.getSourceId());
                log.log(Level.INFO, String.format("downloadMetadata - manga [%d] name [%s] source [%s] source ID [%s] URL [%s] - Starting download task.",
                        manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId(), url));

                driver.get(url);

                new WebDriverWait(driver, 300)
                        .ignoring(StaleElementReferenceException.class)
                        .until((WebDriver d) -> {
                            log.log(Level.INFO, String.format("downloadMetadata - manga [%d] name [%s] source [%s] source ID [%s] URL [%s] - Waiting for elements to load.",
                                    manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId(), url));
                            d.findElement(By.className("ShowAllChapters")).click();
                            log.log(Level.INFO, String.format("downloadMetadata - manga [%d] name [%s] source [%s] source ID [%s] URL [%s] - Elements loaded.",
                                    manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId(), url));
                            return true;
                        });

                for (WebElement c : driver.findElements(By.className("ChapterLink"))) {
                    try {
                        int chapterNumber = extractChapterNumber(c.getAttribute("href"));
                        log.log(Level.INFO, String.format("downloadMetadata - manga [%d] name [%s] source [%s] source ID [%s] URL [%s] chapter [%d]- Found chapter.",
                                manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId(), url, chapterNumber));
                        if (!manga.getChapters().containsKey(chapterNumber)) {
                            Chapter chapter = new Chapter(chapterNumber, "Chapter " + chapterNumber);
                            manga.getChapters().put(chapter.getNumber(), chapter);
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }

                log.log(Level.INFO, String.format("downloadMetadata - manga [%d] name [%s] source [%s] source ID [%s] URL [%s] chapters [%d]- Finished download task.",
                        manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId(), url, manga.getChapters().size()));
                callback.accept(manga);
            } catch (Exception e) {
                log.log(Level.SEVERE, String.format("downloadMetadata - manga [%d] name [%s] source [%s] source ID [%s]- Error encountered.",
                        manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId()), e);
            }
        });
    }

    private int extractChapterNumber(String url) {
        int beginIndex = url.indexOf("-chapter-") + 9;
        int endIndex = url.indexOf("-page-");
        return Integer.parseInt(url.substring(beginIndex, endIndex));
    }

    @Override
    public void downloadChapter(Manga manga, int chapterNumber, Consumer<Chapter> callback) {
        log.log(Level.INFO, String.format("downloadChapter - manga [%d] name [%s] source [%s] source ID [%s] chapter [%d] - Queuing download task.",
                manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId(), chapterNumber));
        executor.submit(() -> {
            try {
                log.log(Level.INFO, String.format("downloadChapter - manga [%d] name [%s] source [%s] source ID [%s] chapter [%d] - Starting download task.",
                        manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId(), chapterNumber));

                Chapter chapter = manga.getChapters().get(chapterNumber);
                int pageNumber = 1;

                while (true) {
                    log.log(Level.INFO, String.format("downloadChapter - manga [%d] name [%s] source [%s] source ID [%s] chapter [%d] page [%d] - Starting to download page.",
                            manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId(), chapterNumber, pageNumber));

                    if (chapter.getPages().containsKey(pageNumber)) {
                        pageNumber++;
                        continue;
                    }

                    String url = String.format(PAGE_URL, manga.getSourceId(), chapterNumber, pageNumber);
                    log.log(Level.INFO, String.format("downloadChapter - manga [%d] name [%s] source [%s] source ID [%s] chapter [%d] page [%d] URL [%s] - Downloading page.",
                            manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId(), chapterNumber, pageNumber, url));

                    driver.get(String.format(PAGE_URL, manga.getSourceId(), chapterNumber, pageNumber));
                    if ("404 Page Not Found".equals(driver.getTitle())) {
                        log.log(Level.INFO, String.format("downloadChapter - manga [%d] name [%s] source [%s] source ID [%s] chapter [%d] page [%d] URL [%s] - Reached last page.",
                                manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId(), chapterNumber, pageNumber, url));
                        break;
                    }

                    Page page = new Page(pageNumber);

                    new WebDriverWait(driver, 300)
                            .ignoring(StaleElementReferenceException.class)
                            .until((WebDriver d) -> {
                                log.log(Level.INFO, String.format("downloadChapter - manga [%d] name [%s] source [%s] source ID [%s] chapter [%d] page [%d] URL [%s] - Waiting for elements to load.",
                                        manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId(), chapterNumber, page.getNumber(), url));
                                String src = d.findElement(By.className("img-fluid")).getAttribute("src");
                                if (src == null) {
                                    return false;
                                }
                                log.log(Level.INFO, String.format("downloadChapter - manga [%d] name [%s] source [%s] source ID [%s] chapter [%d] page [%d] URL [%s] - Elements loaded.",
                                        manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId(), chapterNumber, page.getNumber(), url));

                                try {
                                    BufferedImage image = ImageIO.read(new URL(src));
                                    page.setData(image);
                                    chapter.getPages().put(page.getNumber(), page);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return true;
                            });

                    log.log(Level.INFO, String.format("downloadChapter - manga [%d] name [%s] source [%s] source ID [%s] chapter [%d] page [%d] - Finished downloading page.",
                            manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId(), chapterNumber, pageNumber));
                    pageNumber++;
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                chapter.setDownloaded(true);
                log.log(Level.INFO, String.format("downloadChapter - manga [%d] name [%s] source [%s] source ID [%s] chapter [%d] pages [%d] - Finished download task.",
                        manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId(), chapterNumber, chapter.getPages().size()));
                callback.accept(chapter);
            } catch (Exception e) {
                log.log(Level.SEVERE, String.format("downloadMetadata - manga [%d] name [%s] source [%s] source ID [%s] chapter [%d] - Error encountered.",
                        manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId(), chapterNumber), e);
            }
        });
    }
}
