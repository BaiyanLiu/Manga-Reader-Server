package com.baiyanliu.mangareader.messaging;

import com.baiyanliu.mangareader.controller.ChapterProcessor;
import com.baiyanliu.mangareader.entity.Chapter;
import com.baiyanliu.mangareader.entity.Manga;
import lombok.Getter;
import org.springframework.hateoas.EntityModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class ChapterMessage extends Message {
    private final Manga manga;
    private final List<EntityModel<Chapter>> chapters = new ArrayList<>();
    private final UpdateType updateType;

    public ChapterMessage(Manga manga, Collection<Chapter> chapters, UpdateType updateType) {
        this.manga = manga;
        for (Chapter chapter : chapters) {
            this.chapters.add(EntityModel.of(chapter, ChapterProcessor.generateLinks(chapter)));
        }
        this.updateType = updateType;
    }

    @Override
    protected String getDestination() {
        return "/chapter";
    }

    @Override
    protected Object prepareForSend() {
        return EntityModel.of(this);
    }
}
