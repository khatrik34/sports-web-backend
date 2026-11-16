package com.sportsweb.backend.homepage;

import com.sportsweb.backend.homepage.dto.HomepageSectionResponse;
import com.sportsweb.backend.homepage.dto.HomepageUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/homepage")
@Tag(name = "Admin · Homepage")
@SecurityRequirement(name = "bearerAuth")
public class HomepageAdminController {

    private final HomepageService homepageService;

    public HomepageAdminController(HomepageService homepageService) {
        this.homepageService = homepageService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('homepage:read')")
    @Operation(summary = "Read the homepage configuration, including disabled blocks")
    public List<HomepageSectionResponse> list() {
        return homepageService.adminList();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('homepage:write')")
    @Operation(summary = "Replace the whole homepage configuration (ordered blocks)")
    public List<HomepageSectionResponse> replaceAll(@Valid @RequestBody HomepageUpdateRequest request) {
        return homepageService.replaceAll(request);
    }
}
