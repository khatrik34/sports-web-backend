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

/** A series or tournament, served at /cricket/series/{slug}. */
@Entity
@Table(name = "cricket_tournament")
public class Tournament extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "slug", nullable = false, length = 200)
    private String slug;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "short_name", length = 60)
    private String shortName;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 24)
    private CricketFormat format = CricketFormat.TEST;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 24)
    private CricketCategory category = CricketCategory.INTERNATIONAL;

    @Column(name = "season", length = 40)
    private String season;

    @Column(name = "host", length = 160)
    private String host;

    @Column(name = "dates_text", length = 120)
    private String datesText;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private TournamentStatus status = TournamentStatus.UPCOMING;

    @Column(name = "logo_url", length = 2048)
    private String logoUrl;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "cricket_tournament_team",
            joinColumns = @JoinColumn(name = "tournament_id"),
            inverseJoinColumns = @JoinColumn(name = "team_id"))
    private Set<Team> teams = new LinkedHashSet<>();

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private long version = 0;

    protected Tournament() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CricketFormat getFormat() {
        return format;
    }

    public void setFormat(CricketFormat format) {
        this.format = format;
    }

    public CricketCategory getCategory() {
        return category;
    }

    public void setCategory(CricketCategory category) {
        this.category = category;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDatesText() {
        return datesText;
    }

    public void setDatesText(String datesText) {
        this.datesText = datesText;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public TournamentStatus getStatus() {
        return status;
    }

    public void setStatus(TournamentStatus status) {
        this.status = status;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public Set<Team> getTeams() {
        return teams;
    }

    public void setTeams(Set<Team> teams) {
        this.teams = teams;
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
        if (!(other instanceof Tournament candidate)) {
            return false;
        }
        return id != null && id.equals(candidate.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
