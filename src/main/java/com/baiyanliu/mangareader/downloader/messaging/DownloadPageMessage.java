package com.baiyanliu.mangareader.downloader.messaging;

import com.baiyanliu.mangareader.entity.Manga;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Data
@DiscriminatorValue("Page")
@EqualsAndHashCode(callSuper = true)
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DownloadPageMessage extends DownloadChapterMessage {
    private int page;

    public DownloadPageMessage(Manga manga, MessageStatus status, String chapter, int page) {
        super(manga, status, chapter);
        this.page = page;
    }
}
