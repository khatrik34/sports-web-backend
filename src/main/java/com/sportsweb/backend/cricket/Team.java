package com.sportsweb.backend.cricket;

import com.sportsweb.backend.common.domain.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * A team the website can show at /teams/{slug}. Teams are deactivated, never deleted, because
 * matches and standings point at them.
 */
@Entity
@Table(name = "cricket_team")
public class Team extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "slug", nullable = false, length = 200)
    private String slug;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "short_name", nullable = false, length = 24)
    private String shortName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 24)
    private CricketCategory category = CricketCategory.INTERNATIONAL;

    @Column(name = "country_code", length = 8)
    private String countryCode;

    @Column(name = "flag_emoji", length = 32)
    private String flag;

    @Column(name = "sport", nullable = false, length = 32)
    private String sport = "cricket";

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "logo_url", length = 2048)
    private String logoUrl;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private long version = 0;

    protected Team() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public CricketCategory getCategory() {
        return category;
    }

    public void setCategory(CricketCategory category) {
        this.category = category;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Team candidate)) {
            return false;
        }
        return id != null && id.equals(candidate.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
