package com.baiyanliu.mangareader.messaging;

import com.baiyanliu.mangareader.controller.ChapterProcessor;
import com.baiyanliu.mangareader.entity.Chapter;
import lombok.Getter;
import org.springframework.hateoas.EntityModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChapterUpdateMessage extends Message {
    @Getter private final Long mangaId;
    @Getter private final List<EntityModel<Chapter>> chapters = new ArrayList<>();

    public ChapterUpdateMessage(Long mangaId, Collection<Chapter> chapters) {
        this.mangaId = mangaId;
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
