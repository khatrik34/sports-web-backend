package com.sportsweb.backend.cricket.admin;

import com.sportsweb.backend.common.audit.AuditAction;
import com.sportsweb.backend.common.audit.AuditService;
import com.sportsweb.backend.common.error.ApiException;
import com.sportsweb.backend.common.util.SlugResolver;
import com.sportsweb.backend.common.util.Texts;
import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.common.web.Pageables;
import com.sportsweb.backend.cricket.CricketMapper;
import com.sportsweb.backend.cricket.Player;
import com.sportsweb.backend.cricket.PlayerRepository;
import com.sportsweb.backend.cricket.PlayerSpecifications;
import com.sportsweb.backend.cricket.Team;
import com.sportsweb.backend.cricket.TeamRepository;
import com.sportsweb.backend.cricket.dto.AdminPlayerResponse;
import com.sportsweb.backend.cricket.dto.PlayerUpsertRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Player profile administration. Players are deactivated rather than deleted. */
@Service
public class PlayerAdminService {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final CricketMapper mapper;
    private final AuditService auditService;

    public PlayerAdminService(PlayerRepository playerRepository,
                              TeamRepository teamRepository,
                              CricketMapper mapper,
                              AuditService auditService) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminPlayerResponse> list(String query, Boolean active, String teamSlug, int page, int size) {
        Specification<Player> specification = (root, criteriaQuery, cb) -> cb.conjunction();
        if (query != null && !query.isBlank()) {
            specification = specification.and(PlayerSpecifications.matchingText(query));
        }
        if (teamSlug != null && !teamSlug.isBlank()) {
            specification = specification.and(PlayerSpecifications.inTeam(teamSlug.trim()));
        }
        if (active != null) {
            specification = specification.and((root, criteriaQuery, cb) -> cb.equal(root.get("active"), active));
        }
        Page<Player> result = playerRepository.findAll(specification, Pageables.of(page, size, Sort.by("fullName")));
        return PageResponse.from(result, mapper::toAdminPlayer);
    }

    @Transactional(readOnly = true)
    public AdminPlayerResponse get(UUID id) {
        return mapper.toAdminPlayer(load(id));
    }

    @Transactional
    public AdminPlayerResponse create(PlayerUpsertRequest request) {
        Player player = new Player();
        player.setSlug(SlugResolver.forCreate(request.slug(), request.fullName(), playerRepository::existsBySlug));
        applyEditableFields(player, request, true);
        playerRepository.save(player);
        auditService.record(AuditAction.PLAYER_CREATED, "cricket_player", player.getId().toString(),
                "Created player '" + player.getFullName() + "'", null, snapshot(player));
        return mapper.toAdminPlayer(player);
    }

    @Transactional
    public AdminPlayerResponse update(UUID id, PlayerUpsertRequest request) {
        Player player = load(id);
        Map<String, Object> before = snapshot(player);
        player.setSlug(SlugResolver.forUpdate(request.slug(), player.getSlug(),
                candidate -> playerRepository.existsBySlugAndIdNot(candidate, id)));
        applyEditableFields(player, request, false);
        playerRepository.save(player);
        auditService.record(AuditAction.PLAYER_UPDATED, "cricket_player", id.toString(),
                "Updated player '" + player.getFullName() + "'", before, snapshot(player));
        return mapper.toAdminPlayer(player);
    }

    @Transactional
    public AdminPlayerResponse setActive(UUID id, boolean active) {
        Player player = load(id);
        if (player.isActive() == active) {
            return mapper.toAdminPlayer(player);
        }
        Map<String, Object> before = snapshot(player);
        player.setActive(active);
        playerRepository.save(player);
        auditService.record(AuditAction.PLAYER_STATUS_CHANGED, "cricket_player", id.toString(),
                (active ? "Activated" : "Deactivated") + " player '" + player.getFullName() + "'",
                before, snapshot(player));
        return mapper.toAdminPlayer(player);
    }

    private void applyEditableFields(Player player, PlayerUpsertRequest request, boolean creating) {
        player.setFullName(request.fullName().trim());
        player.setTeam(resolveTeam(request.teamId()));
        player.setCountry(Texts.blankToNull(request.country()));
        player.setRole(Texts.blankToNull(request.role()));
        player.setBattingStyle(Texts.blankToNull(request.battingStyle()));
        player.setBowlingStyle(Texts.blankToNull(request.bowlingStyle()));
        player.setBornOn(request.bornOn());
        player.setImageUrl(Texts.blankToNull(request.imageUrl()));
        player.setDescription(Texts.blankToNull(request.description()));
        if (request.active() != null) {
            player.setActive(request.active());
        } else if (creating) {
            player.setActive(true);
        }
    }

    private Team resolveTeam(UUID teamId) {
        if (teamId == null) {
            return null;
        }
        return teamRepository.findById(teamId)
                .orElseThrow(() -> ApiException.badRequest("invalid_reference", "Team was not found."));
    }

    private Player load(UUID id) {
        return playerRepository.findById(id).orElseThrow(() -> ApiException.notFound("Player"));
    }

    private Map<String, Object> snapshot(Player player) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("slug", player.getSlug());
        values.put("fullName", player.getFullName());
        values.put("teamId", player.getTeam() == null ? null : player.getTeam().getId());
        values.put("role", player.getRole());
        values.put("active", player.isActive());
        return values;
    }
}
