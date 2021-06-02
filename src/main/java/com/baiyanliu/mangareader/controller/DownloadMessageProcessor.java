package com.baiyanliu.mangareader.controller;

import com.baiyanliu.mangareader.downloader.messaging.DownloadMessage;
import com.baiyanliu.mangareader.downloader.messaging.Status;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class DownloadMessageProcessor implements RepresentationModelProcessor<EntityModel<DownloadMessage>> {

    public static Collection<Link> generateLinks(DownloadMessage message) {
        List<Link> links = new ArrayList<>();
        if (message.getStatus() == Status.STARTED) {
            links.add(linkTo(methodOn(DownloadMessageController.class).cancelDownload(message.getId())).withRel("cancel"));
        } else if (message.getStatus() == Status.ERROR) {
            links.add(linkTo(methodOn(DownloadMessageController.class).resolveError(message.getId())).withRel("resolve"));
        }
        return links;
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
