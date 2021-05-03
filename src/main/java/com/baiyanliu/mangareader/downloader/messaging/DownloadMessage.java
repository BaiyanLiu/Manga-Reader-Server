package com.baiyanliu.mangareader.downloader.messaging;

import com.baiyanliu.mangareader.entity.Manga;
import com.baiyanliu.mangareader.messaging.Message;
import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@Inheritance
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RestResource(path="downloadMessages")
public abstract class DownloadMessage extends Message {
    @GeneratedValue(generator = "downloadMessageId") @Id private long id;

    @ManyToOne private Manga manga;
    private int total;
    private int completed;

    public DownloadMessage(Manga manga) {
        super(new Date());
        this.manga = manga;
        total = 1;
    }

    @JsonGetter(value = "mangaName")
    @Transient
    public String getMangaName() {
        return manga.getName();
    }

    @Override
    protected String getDestination() {
        return "/download";
    }
}
