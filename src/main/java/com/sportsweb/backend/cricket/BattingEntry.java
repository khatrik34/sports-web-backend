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

/** One batter's row in an innings scorecard. Names are free text so club cricket works too. */
@Entity
@Table(name = "cricket_batting_entry")
public class BattingEntry extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "innings_id", nullable = false)
    private Innings innings;

    @Column(name = "position", nullable = false)
    private int position = 0;

    @Column(name = "batter", nullable = false, length = 120)
    private String batter;

    @Column(name = "dismissal", length = 120)
    private String dismissal;

    @Column(name = "runs", nullable = false)
    private int runs = 0;

    @Column(name = "balls", nullable = false)
    private int balls = 0;

    @Column(name = "fours", nullable = false)
    private int fours = 0;

    @Column(name = "sixes", nullable = false)
    private int sixes = 0;

    @Column(name = "not_out", nullable = false)
    private boolean notOut = false;

    @Version
    @Column(name = "version", nullable = false)
    private long version = 0;

    public BattingEntry() {
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

    public String getBatter() {
        return batter;
    }

    public void setBatter(String batter) {
        this.batter = batter;
    }

    public String getDismissal() {
        return dismissal;
    }

    public void setDismissal(String dismissal) {
        this.dismissal = dismissal;
    }

    public int getRuns() {
        return runs;
    }

    public void setRuns(int runs) {
        this.runs = runs;
    }

    public int getBalls() {
        return balls;
    }

    public void setBalls(int balls) {
        this.balls = balls;
    }

    public int getFours() {
        return fours;
    }

    public void setFours(int fours) {
        this.fours = fours;
    }

    public int getSixes() {
        return sixes;
    }

    public void setSixes(int sixes) {
        this.sixes = sixes;
    }

    public boolean isNotOut() {
        return notOut;
    }

    public void setNotOut(boolean notOut) {
        this.notOut = notOut;
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
        if (!(other instanceof BattingEntry candidate)) {
            return false;
        }
        return id != null && id.equals(candidate.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
