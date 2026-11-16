package com.sportsweb.backend.news;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NewsCategoryRepository extends JpaRepository<NewsCategory, UUID> {

    Optional<NewsCategory> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    List<NewsCategory> findAllByOrderBySortOrderAscNameAsc();
}
