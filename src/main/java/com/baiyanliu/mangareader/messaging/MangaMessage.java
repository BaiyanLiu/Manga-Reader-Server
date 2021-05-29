package com.baiyanliu.mangareader.messaging;

import com.baiyanliu.mangareader.entity.Manga;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MangaMessage extends Message {
    private final Manga manga;

    @Override
    protected String getDestination() {
        return "/manga";
    }
}
