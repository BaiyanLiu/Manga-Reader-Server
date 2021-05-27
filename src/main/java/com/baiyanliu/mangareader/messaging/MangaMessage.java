package com.baiyanliu.mangareader.messaging;

import com.baiyanliu.mangareader.entity.Manga;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MangaMessage extends Message {
    @Getter private final Manga manga;

    @Override
    protected String getDestination() {
        return "/manga";
    }
}
