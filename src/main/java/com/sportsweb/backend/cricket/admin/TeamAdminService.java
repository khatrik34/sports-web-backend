package com.sportsweb.backend.cricket.admin;

import com.sportsweb.backend.common.audit.AuditAction;
import com.sportsweb.backend.common.audit.AuditService;
import com.sportsweb.backend.common.error.ApiException;
import com.sportsweb.backend.common.util.SlugResolver;
import com.sportsweb.backend.common.util.Texts;
import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.common.web.Pageables;
import com.sportsweb.backend.cricket.CricketCategory;
import com.sportsweb.backend.cricket.CricketMapper;
import com.sportsweb.backend.cricket.Team;
import com.sportsweb.backend.cricket.TeamRepository;
import com.sportsweb.backend.cricket.TeamSpecifications;
import com.sportsweb.backend.cricket.dto.AdminTeamResponse;
import com.sportsweb.backend.cricket.dto.TeamUpsertRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Team administration. Teams are never deleted, only deactivated, because matches point at them. */
@Service
public class TeamAdminService {

    private final TeamRepository teamRepository;
    private final CricketMapper mapper;
    private final AuditService auditService;

    public TeamAdminService(TeamRepository teamRepository, CricketMapper mapper, AuditService auditService) {
        this.teamRepository = teamRepository;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminTeamResponse> list(String query, Boolean active, int page, int size) {
        Specification<Team> specification = (root, criteriaQuery, cb) -> cb.conjunction();
        specification = specification.and(TeamSpecifications.withActiveState(active));
        if (query != null && !query.isBlank()) {
            specification = specification.and(TeamSpecifications.matchingText(query));
        }
        Page<Team> result = teamRepository.findAll(specification, Pageables.of(page, size, Sort.by("name")));
        return PageResponse.from(result, mapper::toAdminTeam);
    }

    @Transactional(readOnly = true)
    public AdminTeamResponse get(UUID id) {
        return mapper.toAdminTeam(load(id));
    }

    @Transactional
    public AdminTeamResponse create(TeamUpsertRequest request) {
        Team team = new Team();
        team.setSlug(SlugResolver.forCreate(request.slug(), request.name(), teamRepository::existsBySlug));
        applyEditableFields(team, request, true);
        teamRepository.save(team);
        auditService.record(AuditAction.TEAM_CREATED, "cricket_team", team.getId().toString(),
                "Created team '" + team.getName() + "'", null, snapshot(team));
        return mapper.toAdminTeam(team);
    }

    @Transactional
    public AdminTeamResponse update(UUID id, TeamUpsertRequest request) {
        Team team = load(id);
        Map<String, Object> before = snapshot(team);
        team.setSlug(SlugResolver.forUpdate(request.slug(), team.getSlug(),
                candidate -> teamRepository.existsBySlugAndIdNot(candidate, id)));
        applyEditableFields(team, request, false);
        teamRepository.save(team);
        auditService.record(AuditAction.TEAM_UPDATED, "cricket_team", id.toString(),
                "Updated team '" + team.getName() + "'", before, snapshot(team));
        return mapper.toAdminTeam(team);
    }

    @Transactional
    public AdminTeamResponse setActive(UUID id, boolean active) {
        Team team = load(id);
        if (team.isActive() == active) {
            return mapper.toAdminTeam(team);
        }
        Map<String, Object> before = snapshot(team);
        team.setActive(active);
        teamRepository.save(team);
        auditService.record(AuditAction.TEAM_STATUS_CHANGED, "cricket_team", id.toString(),
                (active ? "Activated" : "Deactivated") + " team '" + team.getName() + "'", before, snapshot(team));
        return mapper.toAdminTeam(team);
    }

    private void applyEditableFields(Team team, TeamUpsertRequest request, boolean creating) {
        team.setName(request.name().trim());
        team.setShortName(request.shortName().trim());
        team.setCategory(request.category() == null ? CricketCategory.INTERNATIONAL : request.category());
        team.setCountryCode(Texts.blankToNull(request.countryCode()));
        team.setFlag(Texts.blankToNull(request.flag()));
        team.setSport(request.sport() == null || request.sport().isBlank() ? "cricket" : request.sport().trim());
        team.setDescription(Texts.blankToNull(request.description()));
        team.setLogoUrl(Texts.blankToNull(request.logoUrl()));
        if (request.active() != null) {
            team.setActive(request.active());
        } else if (creating) {
            team.setActive(true);
        }
    }

    private Team load(UUID id) {
        return teamRepository.findById(id).orElseThrow(() -> ApiException.notFound("Team"));
    }

    private Map<String, Object> snapshot(Team team) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("slug", team.getSlug());
        values.put("name", team.getName());
        values.put("shortName", team.getShortName());
        values.put("category", team.getCategory());
        values.put("active", team.isActive());
        return values;
    }
}
