package com.baiyanliu.mangareader.messaging;

import com.baiyanliu.mangareader.controller.ChapterProcessor;
import com.baiyanliu.mangareader.entity.Chapter;
import com.baiyanliu.mangareader.entity.Manga;
import lombok.Getter;
import org.springframework.hateoas.EntityModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChapterMessage extends Message {
    @Getter private final Manga manga;
    @Getter private final List<EntityModel<Chapter>> chapters = new ArrayList<>();

    public ChapterMessage(Manga manga, Collection<Chapter> chapters) {
        this.manga = manga;
        for (Chapter chapter : chapters) {
            this.chapters.add(EntityModel.of(chapter, ChapterProcessor.generateLinks(chapter)));
        }
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
