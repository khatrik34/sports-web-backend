package com.sportsweb.backend.homepage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportsweb.backend.common.audit.AuditAction;
import com.sportsweb.backend.common.audit.AuditService;
import com.sportsweb.backend.common.error.ApiException;
import com.sportsweb.backend.common.util.Texts;
import com.sportsweb.backend.homepage.dto.HomepageSectionRequest;
import com.sportsweb.backend.homepage.dto.HomepageSectionResponse;
import com.sportsweb.backend.homepage.dto.HomepageUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Homepage configuration is a small ordered list of typed blocks. It is replaced as a whole so the
 * public page can never read a half-applied layout.
 */
@Service
public class HomepageService {

    private final HomepageSectionRepository repository;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    public HomepageService(HomepageSectionRepository repository,
                           ObjectMapper objectMapper,
                           AuditService auditService) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<HomepageSectionResponse> adminList() {
        return repository.findAllByOrderBySortOrderAsc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<HomepageSectionResponse> replaceAll(HomepageUpdateRequest request) {
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("sections", repository.count());

        Set<String> keys = new LinkedHashSet<>();
        List<HomepageSection> replacements = new ArrayList<>();
        for (HomepageSectionRequest section : request.sections()) {
            String key = Texts.blankToNull(section.key());
            if (key == null) {
                String title = Texts.blankToNull(section.title());
                key = title == null
                        ? "section-" + (replacements.size() + 1)
                        : title.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
            }
            if (!keys.add(key)) {
                throw ApiException.badRequest("duplicate_section", "Section key " + key + " appears twice.");
            }
            if (section.config() != null && !section.config().isObject()) {
                throw ApiException.badRequest("invalid_config", "Section config must be a JSON object.");
            }
            HomepageSection entity = new HomepageSection();
            entity.setSectionKey(key);
            entity.setType(section.type().trim().toUpperCase(Locale.ROOT));
            entity.setTitle(Texts.blankToNull(section.title()));
            entity.setSortOrder(section.sortOrder());
            entity.setEnabled(section.enabled() == null || section.enabled());
            entity.setConfigJson(section.config() == null || section.config().isNull()
                    ? "{}"
                    : section.config().toString());
            replacements.add(entity);
        }

        repository.deleteAll(repository.findAll());
        repository.flush();
        repository.saveAll(replacements);
        repository.flush();

        auditService.record(AuditAction.HOMEPAGE_UPDATED, "homepage_section", null,
                "Replaced homepage configuration (" + replacements.size() + " sections)",
                before, Map.of("sections", replacements.size()));
        return replacements.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<HomepageSectionResponse> publicList() {
        return repository.findByEnabledTrueOrderBySortOrderAsc().stream().map(this::toResponse).toList();
    }

    private HomepageSectionResponse toResponse(HomepageSection section) {
        return new HomepageSectionResponse(section.getSectionKey(), section.getType(), section.getTitle(),
                section.getSortOrder(), section.isEnabled(), readConfig(section.getConfigJson()));
    }

    private JsonNode readConfig(String json) {
        try {
            return objectMapper.readTree(json == null || json.isBlank() ? "{}" : json);
        } catch (Exception ex) {
            return objectMapper.createObjectNode();
        }
    }
}
