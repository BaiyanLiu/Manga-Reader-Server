package com.baiyanliu.mangareader.messaging;

import com.baiyanliu.mangareader.entity.Chapter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ChapterUpdateMessage extends Message {
    @Getter private final Long mangaId;
    @Getter private final Chapter chapter;

    @Override
    protected String getDestination() {
        return "/chapter";
    }
}
