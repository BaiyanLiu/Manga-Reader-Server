package com.baiyanliu.mangareader.downloader.messaging;

import com.baiyanliu.mangareader.entity.Manga;
import lombok.Getter;

public class DownloadChapterMessage extends DownloadMessage {
    @Getter private final String chapter;

    public DownloadChapterMessage(Manga manga, MessageStatus status, String chapter) {
        super(manga, status);
        this.chapter = chapter;
    }

    @Override
    protected String getDestination() {
        return "/download/chapter";
    }
}
