package com.baiyanliu.mangareader.entity.repository;

import com.baiyanliu.mangareader.entity.Manga;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MangaRepository extends PagingAndSortingRepository<Manga, Long> {}
