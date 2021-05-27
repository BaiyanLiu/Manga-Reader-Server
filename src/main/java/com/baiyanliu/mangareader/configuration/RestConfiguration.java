package com.baiyanliu.mangareader.configuration;

import com.baiyanliu.mangareader.downloader.messaging.DownloadChapterMessage;
import com.baiyanliu.mangareader.downloader.messaging.DownloadMetadataMessage;
import com.baiyanliu.mangareader.downloader.messaging.UpdateMessage;
import com.baiyanliu.mangareader.entity.Manga;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
class RestConfiguration implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        config.exposeIdsFor(Manga.class);
        config.exposeIdsFor(DownloadMetadataMessage.class);
        config.exposeIdsFor(DownloadChapterMessage.class);
        config.exposeIdsFor(UpdateMessage.class);
    }
}
