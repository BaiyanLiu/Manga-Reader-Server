package com.baiyanliu.mangareader.downloader;

import com.baiyanliu.mangareader.CustomLogger;
import com.baiyanliu.mangareader.downloader.messaging.DownloadMessage;
import com.baiyanliu.mangareader.downloader.messaging.DownloadMessageHelper;
import com.baiyanliu.mangareader.entity.Chapter;
import com.baiyanliu.mangareader.entity.Page;
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
import java.io.InterruptedIOException;
import java.net.URL;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

abstract class WebDriverDownloader extends Downloader<WebDriver> {
    private static final int WEB_DRIVER_TIMEOUT = 30;

    private final ChromeDriverService chromeDriverService;
    private final ChromeOptions chromeOptions;

    protected WebDriverDownloader(DownloadMessageHelper downloadMessageHelper, TaskManager taskManager) {
        super(downloadMessageHelper, taskManager);
        chromeDriverService = new ChromeDriverService.Builder().withSilent(true).build();
        chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless", "--disable-gpu", "--ignore-certificate-errors");
    }

    @Override
    protected final WebDriver initDriver() {
        return new ChromeDriver(chromeDriverService, chromeOptions);
    }

    @Override
    protected final void cleanUpDriver(WebDriver driver) {
        driver.quit();
    }

    @Override
    protected final void prepareDriver(WebDriver driver, String url, CustomLogger logger) throws Exception {
        driver.get(url);

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
            throw (e.getCause() instanceof InterruptedException || e.getCause() instanceof InterruptedIOException ? (Exception) e.getCause() : e);
        }
    }

    protected abstract void loadChapters(WebDriver driver);

    @Override
    protected final void downloadPage(WebDriver driver, Chapter chapter, Page page, DownloadMessage message, String url, CustomLogger logger,
                                Consumer<Integer> lastPageCallback, Function<BufferedImage, Boolean> pageImageCallback) throws Exception {
        driver.get(url);

        try {
            new WebDriverWait(driver, WEB_DRIVER_TIMEOUT)
                    .ignoring(ElementNotInteractableException.class, StaleElementReferenceException.class)
                    .until((WebDriver d) -> {
                        logger.log(Level.INFO, "Waiting for elements to load", String.format("page [%d] pages [%d] URL [%s] ", page.getNumber(), chapter.getLastPage(), url));

                        if (page.getNumber() == 1) {
                            lastPageCallback.accept(getLastPage(d));
                        }

                        String imageSource = getImageSource(d);
                        if (imageSource == null) {
                            return false;
                        }

                        logger.log(Level.INFO, "Elements loaded", String.format("page [%d] pages [%d] URL [%s] ", page.getNumber(), chapter.getLastPage(), url));

                        try {
                            BufferedImage image = ImageIO.read(new URL(imageSource));
                            return pageImageCallback.apply(image);
                        } catch (IOException e) {
                            return false;
                        }
                    });
        } catch (WebDriverException e) {
            throw (e.getCause() instanceof InterruptedException || e.getCause() instanceof InterruptedIOException ? (Exception) e.getCause() : e);
        }
    }

    protected abstract int getLastPage(WebDriver driver);

    protected abstract String getImageSource(WebDriver driver);
}
