package com.baiyanliu.mangareader.downloader.messaging;

import com.baiyanliu.mangareader.messaging.Message;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import java.util.Date;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@Inheritance
@NoArgsConstructor(access = AccessLevel.PROTECTED)
abstract class DownloadMessage extends Message {
    @GeneratedValue(generator = "downloadMessageId") @Id private long id;

    private String manga;
    private MessageStatus status;

    public DownloadMessage(String manga, MessageStatus status) {
        super(new Date());
        this.manga = manga;
        this.status = status;
    }
}
