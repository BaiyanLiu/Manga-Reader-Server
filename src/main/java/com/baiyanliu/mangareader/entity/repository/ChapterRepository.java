package com.baiyanliu.mangareader.entity.repository;

import com.baiyanliu.mangareader.entity.Chapter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface ChapterRepository extends CrudRepository<Chapter, Long> {}
