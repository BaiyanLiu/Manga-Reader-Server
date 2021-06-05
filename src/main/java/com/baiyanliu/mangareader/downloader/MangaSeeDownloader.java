package com.baiyanliu.mangareader.downloader;

import com.baiyanliu.mangareader.downloader.messaging.DownloadMessageHelper;
import com.baiyanliu.mangareader.entity.Manga;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

class MangaSeeDownloader extends WebDriverDownloader {
    private static final String ROOT_URL = "https://mangasee123.com";
    private static final String METADATA_URL = ROOT_URL + "/manga/%s";
    private static final String PAGE_URL = ROOT_URL + "/read-online/%s-chapter-%s-page-%d.html";

    public MangaSeeDownloader(DownloadMessageHelper downloadMessageHelper, TaskManager taskManager) {
        super(downloadMessageHelper, taskManager);
    }

    @Override
    protected String getMetadataUrl(Manga manga) {
        return String.format(METADATA_URL, manga.getSourceId());
    }

    @Override
    protected void loadChapters(WebDriver driver) {
        driver.findElement(By.className("ShowAllChapters")).click();
    }

    @Override
    protected List<String> getChapterNumbers(WebDriver driver) {
        List<String> chapterNumbers = new ArrayList<>();
        for (WebElement c : driver.findElements(By.className("ChapterLink"))) {
            String url = c.getAttribute("href");
            int beginIndex = url.indexOf("-chapter-") + 9;
            int endIndex = url.indexOf("-page-");
            chapterNumbers.add(url.substring(beginIndex, endIndex));
        }
        return chapterNumbers;
    }

    @Override
    protected String getPageUrl(Manga manga, String chapterNumber, int pageNumber) {
        return String.format(PAGE_URL, manga.getSourceId(), chapterNumber, pageNumber);
    }

    @Override
    protected int getLastPage(WebDriver driver) {
        driver.findElement(By.cssSelector("button[data-target='#PageModal']")).click();
        List<WebElement> elements = driver.findElements(By.cssSelector("button[ng-click='vm.GoToPage(Page)']"));
        return elements.size();
    }

    @Override
    protected String getImageSource(WebDriver driver) {
        return driver.findElement(By.className("img-fluid")).getAttribute("src");
    }
}
