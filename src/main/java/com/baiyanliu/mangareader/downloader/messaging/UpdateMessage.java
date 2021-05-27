package com.baiyanliu.mangareader.downloader.messaging;

import com.baiyanliu.mangareader.entity.Manga;
import com.baiyanliu.mangareader.messaging.LogMessage;
import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateMessage extends LogMessage {
    @GeneratedValue(generator = "updateMessageId") @Id private long id;

    @ManyToOne @OnDelete(action = OnDeleteAction.CASCADE) private Manga manga;
    private String chapter;

    public UpdateMessage(Manga manga, String chapter) {
        super(new Date());
        this.manga = manga;
        this.chapter = chapter;
    }

    @JsonGetter(value = "mangaName")
    @Transient
    public String getMangaName() {
        return manga.getName();
    }

    @Override
    protected String getDestination() {
        return "/update";
    }
}
