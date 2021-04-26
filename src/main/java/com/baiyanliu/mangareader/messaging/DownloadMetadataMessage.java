package com.baiyanliu.mangareader.messaging;

import com.baiyanliu.mangareader.entity.Manga;

public class DownloadMetadataMessage extends DownloadMessage {

    public DownloadMetadataMessage(Manga manga, MessageStatus status) {
        super(manga, status);
    }

    @Override
    protected String getDestination() {
        return "/download/metadata";
    }
}
