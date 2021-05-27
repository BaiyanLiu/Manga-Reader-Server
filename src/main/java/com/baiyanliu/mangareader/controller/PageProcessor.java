package com.baiyanliu.mangareader.controller;

import com.baiyanliu.mangareader.entity.Chapter;
import com.baiyanliu.mangareader.entity.Page;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PageProcessor implements RepresentationModelProcessor<EntityModel<Page>> {

    @NonNull
    @Override
    public EntityModel<Page> process(@NonNull EntityModel<Page> model) {
        Page page = model.getContent();
        if (page != null) {
            Chapter chapter = page.getChapter();
            Long mangaId = chapter.getManga().getId();
            int pageNumber = page.getNumber();

            if (pageNumber > 1) {
                model.add(linkTo(methodOn(MangaController.class).getOnePage(mangaId, chapter.getNumber(), pageNumber - 1)).withRel("prev"));
            }
            if (pageNumber != chapter.getLastPage()) {
                model.add(linkTo(methodOn(MangaController.class).getOnePage(mangaId, chapter.getNumber(), pageNumber + 1)).withRel("next"));
            }
        }
        return model;
    }
}
