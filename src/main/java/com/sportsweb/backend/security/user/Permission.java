package com.sportsweb.backend.security.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

/** Fine grained capability, e.g. {@code news:publish}. Rows are seeded by migration V2. */
@Entity
@Table(name = "app_permission")
public class Permission {

    @Id
    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    protected Permission() {
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Permission permission)) {
            return false;
        }
        return code != null && code.equals(permission.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }
}
