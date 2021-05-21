package com.baiyanliu.mangareader.downloader.messaging;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface DownloadMessageRepository extends PagingAndSortingRepository<DownloadMessage, Long> {

    Iterable<DownloadMessage> findAllByStatus(Status status);
}
