package com.sportsweb.backend.homepage.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** The homepage is replaced as a whole, so it can never be left half-configured. */
public record HomepageUpdateRequest(@NotNull @Valid List<HomepageSectionRequest> sections) {
}
