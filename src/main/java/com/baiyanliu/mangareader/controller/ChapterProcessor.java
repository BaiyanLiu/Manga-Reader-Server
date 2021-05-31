package com.baiyanliu.mangareader.controller;

import com.baiyanliu.mangareader.entity.Chapter;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ChapterProcessor implements RepresentationModelProcessor<EntityModel<Chapter>> {

    public static Link[] generateLinks(Chapter chapter) {
        Long mangaId = chapter.getManga().getId();
        String chapterNumber = chapter.getNumber();
        return new Link[] {
                linkTo(methodOn(MangaController.class).getPage(mangaId, chapterNumber, 1)).withRel("firstPage"),
                linkTo(methodOn(MangaController.class).downloadChapter(mangaId, chapterNumber)).withRel("download"),
                linkTo(methodOn(MangaController.class).ignoreChapter(mangaId, chapterNumber)).withRel("ignore"),
                linkTo(methodOn(MangaController.class).deleteChapter(mangaId, chapterNumber)).withRel("delete")
        };
    }

    @NonNull
    @Override
    public EntityModel<Chapter> process(@NonNull EntityModel<Chapter> model) {
        Chapter chapter = model.getContent();
        if (chapter != null) {
            model.add(generateLinks(chapter));
        }
        return model;
    }
}
