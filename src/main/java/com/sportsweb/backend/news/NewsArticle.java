package com.sportsweb.backend.news;

import com.sportsweb.backend.common.domain.AuditedEntity;
import com.sportsweb.backend.security.user.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * News article with its editorial state and SEO metadata.
 *
 * <p>The public API only ever returns articles where {@link #isPubliclyVisible(Instant)} is
 * true; drafts, archived articles and future-published articles never leave the admin API.</p>
 */
@Entity
@Table(name = "news_article")
public class NewsArticle extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "slug", nullable = false, length = 200)
    private String slug;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "subtitle", length = 400)
    private String subtitle;

    @Column(name = "summary", length = 1000)
    private String summary;

    @Column(name = "body", nullable = false, columnDefinition = "text")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "body_format", nullable = false, length = 16)
    private BodyFormat bodyFormat = BodyFormat.HTML;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NewsStatus status = NewsStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private NewsCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id")
    private UserAccount author;

    @Column(name = "author_display_name", length = 120)
    private String authorDisplayName;

    @Column(name = "cover_image_url", length = 2048)
    private String coverImageUrl;

    @Column(name = "cover_image_alt", length = 300)
    private String coverImageAlt;

    @Column(name = "meta_title", length = 200)
    private String metaTitle;

    @Column(name = "meta_description", length = 400)
    private String metaDescription;

    @Column(name = "canonical_path", length = 500)
    private String canonicalPath;

    @Column(name = "og_title", length = 200)
    private String ogTitle;

    @Column(name = "og_description", length = 400)
    private String ogDescription;

    @Column(name = "og_image_url", length = 2048)
    private String ogImageUrl;

    @Column(name = "robots", nullable = false, length = 64)
    private String robots = "index,follow";

    @Column(name = "keywords", length = 500)
    private String keywords;

    @Column(name = "featured", nullable = false)
    private boolean featured;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "archived_at")
    private Instant archivedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected NewsArticle() {
    }

    /** Single source of truth for "may the public see this article?" */
    public boolean isPubliclyVisible(Instant now) {
        return status == NewsStatus.PUBLISHED
                && publishedAt != null
                && !publishedAt.isAfter(now);
    }

    public UUID getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public BodyFormat getBodyFormat() {
        return bodyFormat;
    }

    public void setBodyFormat(BodyFormat bodyFormat) {
        this.bodyFormat = bodyFormat;
    }

    public NewsStatus getStatus() {
        return status;
    }

    public void setStatus(NewsStatus status) {
        this.status = status;
    }

    public NewsCategory getCategory() {
        return category;
    }

    public void setCategory(NewsCategory category) {
        this.category = category;
    }

    public UserAccount getAuthor() {
        return author;
    }

    public void setAuthor(UserAccount author) {
        this.author = author;
    }

    public String getAuthorDisplayName() {
        return authorDisplayName;
    }

    public void setAuthorDisplayName(String authorDisplayName) {
        this.authorDisplayName = authorDisplayName;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getCoverImageAlt() {
        return coverImageAlt;
    }

    public void setCoverImageAlt(String coverImageAlt) {
        this.coverImageAlt = coverImageAlt;
    }

    public String getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getCanonicalPath() {
        return canonicalPath;
    }

    public void setCanonicalPath(String canonicalPath) {
        this.canonicalPath = canonicalPath;
    }

    public String getOgTitle() {
        return ogTitle;
    }

    public void setOgTitle(String ogTitle) {
        this.ogTitle = ogTitle;
    }

    public String getOgDescription() {
        return ogDescription;
    }

    public void setOgDescription(String ogDescription) {
        this.ogDescription = ogDescription;
    }

    public String getOgImageUrl() {
        return ogImageUrl;
    }

    public void setOgImageUrl(String ogImageUrl) {
        this.ogImageUrl = ogImageUrl;
    }

    public String getRobots() {
        return robots;
    }

    public void setRobots(String robots) {
        this.robots = robots;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(Instant archivedAt) {
        this.archivedAt = archivedAt;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof NewsArticle article)) {
            return false;
        }
        return id != null && id.equals(article.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
