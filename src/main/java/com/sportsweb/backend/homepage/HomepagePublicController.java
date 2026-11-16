package com.sportsweb.backend.homepage;

import com.sportsweb.backend.homepage.dto.HomepageSectionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/homepage")
@Tag(name = "Public · Homepage")
public class HomepagePublicController {

    private final HomepageService homepageService;

    public HomepagePublicController(HomepageService homepageService) {
        this.homepageService = homepageService;
    }

    @GetMapping
    @Operation(summary = "Enabled homepage blocks in display order")
    public List<HomepageSectionResponse> list() {
        return homepageService.publicList();
    }
}
