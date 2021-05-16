package com.baiyanliu.mangareader.controller;

import com.baiyanliu.mangareader.downloader.messaging.DownloadMessage;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class DownloadMessageProcessor implements RepresentationModelProcessor<EntityModel<DownloadMessage>> {

    public static Link[] generateLinks(DownloadMessage message) {
        return new Link[] {linkTo(methodOn(MangaController.class).cancelDownload(message.getId())).withRel("cancel")};
    }

    @NonNull
    @Override
    public EntityModel<DownloadMessage> process(@NonNull EntityModel<DownloadMessage> model) {
        DownloadMessage message = model.getContent();
        if (message != null) {
            model.add(generateLinks(message));
        }
        return model;
    }
}
