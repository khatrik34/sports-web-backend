package com.sportsweb.backend.homepage;

import com.sportsweb.backend.common.domain.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.Objects;
import java.util.UUID;

/**
 * One configured block of the homepage. The block type decides how the website renders it and
 * {@code configJson} carries its references (article slugs, match slugs, series slug, video slugs).
 */
@Entity
@Table(name = "homepage_section")
public class HomepageSection extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "section_key", nullable = false, length = 60)
    private String sectionKey;

    @Column(name = "type", nullable = false, length = 40)
    private String type;

    @Column(name = "title", length = 160)
    private String title;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "config_json", nullable = false, columnDefinition = "text")
    private String configJson = "{}";

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public HomepageSection() {
    }

    public UUID getId() {
        return id;
    }

    public String getSectionKey() {
        return sectionKey;
    }

    public void setSectionKey(String sectionKey) {
        this.sectionKey = sectionKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getConfigJson() {
        return configJson;
    }

    public void setConfigJson(String configJson) {
        this.configJson = configJson;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof HomepageSection candidate)) {
            return false;
        }
        return id != null && id.equals(candidate.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
