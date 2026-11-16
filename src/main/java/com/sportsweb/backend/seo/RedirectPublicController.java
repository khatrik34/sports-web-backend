package com.sportsweb.backend.seo;

import com.sportsweb.backend.seo.dto.PublicRedirect;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/redirects")
@Tag(name = "Public · Redirects")
public class RedirectPublicController {

    private final RedirectService redirectService;

    public RedirectPublicController(RedirectService redirectService) {
        this.redirectService = redirectService;
    }

    @GetMapping
    @Operation(summary = "Resolve one path; 404 when nothing is configured for it")
    public PublicRedirect resolve(@RequestParam String path) {
        return redirectService.resolve(path);
    }
}
