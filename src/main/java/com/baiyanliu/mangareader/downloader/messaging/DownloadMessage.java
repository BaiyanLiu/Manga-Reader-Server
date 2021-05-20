package com.baiyanliu.mangareader.downloader.messaging;

import com.baiyanliu.mangareader.controller.DownloadMessageProcessor;
import com.baiyanliu.mangareader.entity.Manga;
import com.baiyanliu.mangareader.messaging.LogMessage;
import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.hateoas.EntityModel;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@Inheritance
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RestResource(path="downloadMessages")
public abstract class DownloadMessage extends LogMessage {
    @GeneratedValue(generator = "downloadMessageId") @Id private long id;

    @ManyToOne private Manga manga;
    private int total;
    private int completed;
    private Status status;

    public DownloadMessage(Manga manga) {
        super(new Date());
        this.manga = manga;
        total = 1;
        status = Status.STARTED;
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

    @Override
    protected Object prepareForSend() {
        return EntityModel.of(this, DownloadMessageProcessor.generateLinks(this));
    }
}
