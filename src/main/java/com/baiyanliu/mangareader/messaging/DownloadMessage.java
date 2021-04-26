package com.baiyanliu.mangareader.messaging;

import com.baiyanliu.mangareader.entity.Manga;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class DownloadMessage extends Message {
    @Getter private final Manga manga;
    @Getter private final MessageStatus status;
}
