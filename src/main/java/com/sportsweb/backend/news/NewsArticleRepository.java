package com.sportsweb.backend.news;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, UUID>, JpaSpecificationExecutor<NewsArticle> {

    Optional<NewsArticle> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    List<NewsArticle> findTop10ByStatusAndPublishedAtLessThanEqualOrderByPublishedAtDesc(NewsStatus status, Instant now);

    List<NewsArticle> findAllByStatusAndPublishedAtLessThanEqualOrderByPublishedAtDesc(NewsStatus status, Instant now);
}
