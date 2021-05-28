package com.baiyanliu.mangareader.controller;

import com.baiyanliu.mangareader.entity.Manga;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class MangaProcessor implements RepresentationModelProcessor<EntityModel<Manga>> {

    @NonNull
    @Override
    public EntityModel<Manga> process(@NonNull EntityModel<Manga> model) {
        Manga manga = model.getContent();
        if (manga != null) {
            Long mangaId = manga.getId();
            model.add(
                    linkTo(methodOn(MangaController.class).getAllChapters(mangaId)).withRel("chapters"),
                    linkTo(methodOn(MangaController.class).updateMetadata(mangaId)).withRel("update"),
                    linkTo(methodOn(MangaController.class).downloadManga(mangaId)).withRel("download")
            );
        }
        return model;
    }
}
