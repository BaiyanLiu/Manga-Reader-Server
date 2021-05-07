package com.baiyanliu.mangareader.messaging;

import com.baiyanliu.mangareader.entity.Chapter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

@AllArgsConstructor
public class ChapterUpdateMessage extends Message {
    @Getter private final Long mangaId;
    @Getter private final Collection<Chapter> chapters;

    @Override
    protected String getDestination() {
        return "/chapter";
    }
}
