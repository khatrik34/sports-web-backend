package com.sportsweb.backend.news;

import com.sportsweb.backend.news.dto.AdminCategoryResponse;
import com.sportsweb.backend.news.dto.CategoryUpsertRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/news-categories")
@Tag(name = "Admin · News categories")
@SecurityRequirement(name = "bearerAuth")
public class NewsCategoryAdminController {

    private final NewsCategoryService categoryService;

    public NewsCategoryAdminController(NewsCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('news:read')")
    @Operation(summary = "List categories")
    public List<AdminCategoryResponse> list() {
        return categoryService.listForAdmin();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('news:write')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a category")
    public AdminCategoryResponse create(@Valid @RequestBody CategoryUpsertRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('news:write')")
    @Operation(summary = "Update a category")
    public AdminCategoryResponse update(@PathVariable UUID id, @Valid @RequestBody CategoryUpsertRequest request) {
        return categoryService.update(id, request);
    }
}
