package com.baiyanliu.mangareader.entity.repository;

import com.baiyanliu.mangareader.entity.Page;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface PageRepository extends CrudRepository<Page, Long> {}
