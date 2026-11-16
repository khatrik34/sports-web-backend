package com.sportsweb.backend.common.error;

/** A single field level validation problem, safe to expose to clients. */
public record FieldErrorItem(String field, String message) {
}
