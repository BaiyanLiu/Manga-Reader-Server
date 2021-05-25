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
        return new Link[] {
                linkTo(methodOn(MangaController.class).getOnePage(chapter.getManga().getId(), chapter.getNumber(), 1)).withRel("firstPage"),
                linkTo(methodOn(MangaController.class).downloadChapter(chapter.getManga().getId(), chapter.getNumber())).withRel("download"),
                linkTo(methodOn(MangaController.class).ignoreChapter(chapter.getManga().getId(), chapter.getNumber())).withRel("ignore")
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
