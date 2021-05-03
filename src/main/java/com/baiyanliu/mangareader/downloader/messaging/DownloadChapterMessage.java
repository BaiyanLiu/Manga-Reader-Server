package com.baiyanliu.mangareader.downloader.messaging;

import com.baiyanliu.mangareader.entity.Manga;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Data
@DiscriminatorValue("Chapter")
@EqualsAndHashCode(callSuper = true)
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DownloadChapterMessage extends DownloadMessage {
    private String chapter;

    public DownloadChapterMessage(Manga manga, String chapter) {
        super(manga);
        this.chapter = chapter;
    }
}
