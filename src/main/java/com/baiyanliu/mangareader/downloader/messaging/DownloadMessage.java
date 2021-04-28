package com.baiyanliu.mangareader.downloader.messaging;

import com.baiyanliu.mangareader.messaging.Message;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@Inheritance
@NoArgsConstructor(access = AccessLevel.PROTECTED)
abstract class DownloadMessage extends Message {
    @GeneratedValue(generator = "downloadMessageId") @Id private long id;
    @JsonIgnore @Version private long version;

    private String manga;
    private MessageStatus status;

    public DownloadMessage(String manga, MessageStatus status) {
        this.manga = manga;
        this.status = status;
    }
}
