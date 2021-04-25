package com.baiyanliu.mangareader.downloader.messaging;

import com.baiyanliu.mangareader.entity.Manga;
import lombok.Getter;

public class DownloadPageMessage extends DownloadChapterMessage {
    @Getter private final int page;

    public DownloadPageMessage(Manga manga, MessageStatus status, String chapter, int page) {
        super(manga, status, chapter);
        this.page = page;
    }

    @Override
    protected String getDestination() {
        return "/download/page";
    }
}
