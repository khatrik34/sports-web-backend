package com.sportsweb.backend.seo;

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
 * A URL that moved. The website asks the API before rendering a 404, so old links keep working
 * after a slug change or a migration from the previous CMS.
 */
@Entity
@Table(name = "seo_redirect")
public class Redirect extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "from_path", nullable = false, length = 500)
    private String fromPath;

    @Column(name = "to_path", length = 500)
    private String toPath;

    @Column(name = "status_code", nullable = false)
    private int statusCode = 301;

    @Column(name = "note", length = 300)
    private String note;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Redirect() {
    }

    public UUID getId() {
        return id;
    }

    public String getFromPath() {
        return fromPath;
    }

    public void setFromPath(String fromPath) {
        this.fromPath = fromPath;
    }

    public String getToPath() {
        return toPath;
    }

    public void setToPath(String toPath) {
        this.toPath = toPath;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Redirect candidate)) {
            return false;
        }
        return id != null && id.equals(candidate.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
