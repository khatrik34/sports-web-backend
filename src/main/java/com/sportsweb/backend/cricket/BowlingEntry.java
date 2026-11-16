package com.sportsweb.backend.cricket;

import com.sportsweb.backend.common.domain.AuditedEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** One bowler's row in an innings scorecard. */
@Entity
@Table(name = "cricket_bowling_entry")
public class BowlingEntry extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "innings_id", nullable = false)
    private Innings innings;

    @Column(name = "position", nullable = false)
    private int position = 0;

    @Column(name = "bowler", nullable = false, length = 120)
    private String bowler;

    @Column(name = "overs_text", length = 16)
    private String oversText;

    @Column(name = "maidens", nullable = false)
    private int maidens = 0;

    @Column(name = "runs_conceded", nullable = false)
    private int runsConceded = 0;

    @Column(name = "wickets", nullable = false)
    private int wickets = 0;

    @Version
    @Column(name = "version", nullable = false)
    private long version = 0;

    public BowlingEntry() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Innings getInnings() {
        return innings;
    }

    public void setInnings(Innings innings) {
        this.innings = innings;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getBowler() {
        return bowler;
    }

    public void setBowler(String bowler) {
        this.bowler = bowler;
    }

    public String getOversText() {
        return oversText;
    }

    public void setOversText(String oversText) {
        this.oversText = oversText;
    }

    public int getMaidens() {
        return maidens;
    }

    public void setMaidens(int maidens) {
        this.maidens = maidens;
    }

    public int getRunsConceded() {
        return runsConceded;
    }

    public void setRunsConceded(int runsConceded) {
        this.runsConceded = runsConceded;
    }

    public int getWickets() {
        return wickets;
    }

    public void setWickets(int wickets) {
        this.wickets = wickets;
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
        if (!(other instanceof BowlingEntry candidate)) {
            return false;
        }
        return id != null && id.equals(candidate.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
