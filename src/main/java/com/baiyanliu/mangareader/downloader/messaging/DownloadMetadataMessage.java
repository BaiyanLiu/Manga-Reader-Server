package com.baiyanliu.mangareader.downloader.messaging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@DiscriminatorValue("Metadata")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DownloadMetadataMessage extends DownloadMessage {

    public DownloadMetadataMessage(String manga, MessageStatus status) {
        super(manga, status);
    }
}
