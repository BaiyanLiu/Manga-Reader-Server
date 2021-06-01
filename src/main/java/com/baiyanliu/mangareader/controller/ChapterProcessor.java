package com.baiyanliu.mangareader.controller;

import com.baiyanliu.mangareader.entity.Chapter;
import com.google.common.collect.Lists;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ChapterProcessor implements RepresentationModelProcessor<EntityModel<Chapter>> {

    public static Collection<Link> generateLinks(Chapter chapter) {
        Long mangaId = chapter.getManga().getId();
        String chapterNumber = chapter.getNumber();

        List<Link> links = Lists.newArrayList(
                linkTo(methodOn(MangaController.class).getPage(mangaId, chapterNumber, 1)).withRel("firstPage"),
                linkTo(methodOn(MangaController.class).ignoreChapter(mangaId, chapterNumber)).withRel("ignore"),
                linkTo(methodOn(MangaController.class).deleteChapter(mangaId, chapterNumber)).withRel("delete")
        );
        if (!chapter.isOrphaned()) {
            links.add(linkTo(methodOn(MangaController.class).downloadChapter(mangaId, chapterNumber)).withRel("download"));
        }

        return links;
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
