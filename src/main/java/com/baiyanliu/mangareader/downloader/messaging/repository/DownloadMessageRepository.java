package com.baiyanliu.mangareader.downloader.messaging.repository;

import com.baiyanliu.mangareader.downloader.messaging.DownloadMessage;
import com.baiyanliu.mangareader.downloader.messaging.Status;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface DownloadMessageRepository extends PagingAndSortingRepository<DownloadMessage, Long> {

    Iterable<DownloadMessage> findAllByStatus(Status status);
}
