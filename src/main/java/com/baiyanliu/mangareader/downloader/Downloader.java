package com.baiyanliu.mangareader.downloader;

import com.baiyanliu.mangareader.CustomLogger;
import com.baiyanliu.mangareader.downloader.messaging.DownloadMessage;
import com.baiyanliu.mangareader.downloader.messaging.DownloadMessageHelper;
import com.baiyanliu.mangareader.downloader.messaging.Status;
import com.baiyanliu.mangareader.entity.Chapter;
import com.baiyanliu.mangareader.entity.Manga;
import com.baiyanliu.mangareader.entity.Page;
import com.google.common.collect.Sets;
import lombok.extern.java.Log;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

@Log
abstract class Downloader<T> {
    private static final int PAGE_DOWNLOAD_DELAY = 2000;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final DownloadMessageHelper downloadMessageHelper;
    private final TaskManager taskManager;

    protected Downloader(DownloadMessageHelper downloadMessageHelper, TaskManager taskManager) {
        this.downloadMessageHelper = downloadMessageHelper;
        this.taskManager = taskManager;
    }

    protected abstract T initDriver();

    protected void cleanUpDriver(T driver) {}

    public final void downloadMetadata(Manga manga, Consumer<Manga> callback) {
        CustomLogger logger = new CustomLogger(log, String.format("downloadMetadata - manga [%d] name [%s] source [%s] source ID [%s] ",
                manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId()));
        logger.log(Level.INFO, "Queuing download task", "");
        DownloadMessage message = downloadMessageHelper.createDownloadMetadataMessage(manga);

        Future<Void> task = executor.submit(() -> {
            boolean isNew = manga.getChapters().isEmpty();
            T driver = null;
            try {
                String url = getMetadataUrl(manga);
                logger.log(Level.INFO, "Starting download task", String.format("URL [%s] ", url));

                driver = initDriver();
                prepareDriver(driver, url, logger);

                Set<String> oldChapters = new HashSet<>(manga.getChapters().keySet());
                Set<String> newChapters = new HashSet<>();

                for (String chapterNumber : getChapterNumbers(driver)) {
                    logger.log(Level.INFO, "Found chapter", String.format("URL [%s] chapter [%s] ", url, chapterNumber));
                    if (!manga.getChapters().containsKey(chapterNumber)) {
                        Chapter chapter = new Chapter(manga, chapterNumber, "Chapter " + chapterNumber);
                        manga.getChapters().put(chapterNumber, chapter);
                        manga.updateUnread(1);
                        if (!isNew) {
                            downloadMessageHelper.createUpdateMessage(manga, chapterNumber);
                        }
                    }
                    newChapters.add(chapterNumber);
                }

                Set<String> orphanedChapters = Sets.difference(oldChapters, newChapters);
                for (Chapter chapter : manga.getChapters().values()) {
                    chapter.setOrphaned(orphanedChapters.contains(chapter.getNumber()));
                }

                logger.log(Level.INFO, "Finished download task", String.format("URL [%s] chapters [%d] ", url, manga.getChapters().size()));
                downloadMessageHelper.updateCompleted(message, 1);

                callback.accept(manga);
            } catch (InterruptedException | InterruptedIOException e) {
                logger.log(Level.INFO, "Cancelling download task", "");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error encountered", "", e);
                downloadMessageHelper.updateStatus(message, Status.ERROR);
                downloadMessageHelper.createErrorMessage(e.getLocalizedMessage());
            } finally {
                taskManager.removeTask(message);
                if (driver != null) {
                    cleanUpDriver(driver);
                }
            }
            return null;
        });
        taskManager.addTask(message, task);
    }

    protected abstract String getMetadataUrl(Manga manga);

    protected void prepareDriver(T driver, String url, CustomLogger logger) throws Exception {}

    protected abstract List<String> getChapterNumbers(T driver);

    public final void downloadChapter(Manga manga, String chapterNumber, Consumer<Chapter> callback) {
        CustomLogger logger = new CustomLogger(log, String.format("downloadChapter - manga [%d] name [%s] source [%s] source ID [%s] chapter [%s] ",
                manga.getId(), manga.getName(), manga.getSource(), manga.getSourceId(), chapterNumber));
        logger.log(Level.INFO, "Queuing download task", "");
        DownloadMessage message = downloadMessageHelper.createDownloadChapterMessage(manga, chapterNumber);

        Future<Void> task = executor.submit(() -> {
            T driver = null;
            try {
                logger.log(Level.INFO, "Starting download task", "");

                driver = initDriver();
                Chapter chapter = manga.getChapters().get(chapterNumber);
                chapter.getPages().clear();
                int pageNumber = 1;

                while (true) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }

                    String url = getPageUrl(manga, chapterNumber, pageNumber);
                    logger.log(Level.INFO, "Downloading page", String.format("page [%d] pages [%d] URL [%s] ", pageNumber, chapter.getLastPage(), url));

                    Page page = new Page(chapter, pageNumber);
                    downloadPage(driver, chapter, page, message, url, logger,
                            lastPage -> {
                                chapter.setLastPage(lastPage);
                                message.setTotal(lastPage);
                            },
                            bufferedImage -> {
                                try {
                                    page.setImage(bufferedImage);
                                } catch (IOException e) {
                                    return false;
                                }
                                chapter.getPages().put(page.getNumber(), page);
                                return true;
                            });

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
            } catch (InterruptedException | InterruptedIOException e) {
                logger.log(Level.INFO, "Cancelling download task", "");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error encountered", "", e);
                downloadMessageHelper.updateStatus(message, Status.ERROR);
                downloadMessageHelper.createErrorMessage(e.getLocalizedMessage());
            } finally {
                taskManager.removeTask(message);
                if (driver != null) {
                    cleanUpDriver(driver);
                }
            }
            return null;
        });
        taskManager.addTask(message, task);
    }

    protected abstract String getPageUrl(Manga manga, String chapterNumber, int pageNumber);

    protected abstract void downloadPage(T driver, Chapter chapter, Page page, DownloadMessage message, String url, CustomLogger logger,
                                         Consumer<Integer> lastPageCallback, Function<BufferedImage, Boolean> pageImageCallback) throws Exception;
}
