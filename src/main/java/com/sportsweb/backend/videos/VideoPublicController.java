package com.sportsweb.backend.videos;

import com.sportsweb.backend.common.web.PageResponse;
import com.sportsweb.backend.videos.dto.PublicVideo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/videos")
@Tag(name = "Public · Videos")
public class VideoPublicController {

    private final VideoService videoService;

    public VideoPublicController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping
    @Operation(summary = "Published videos; filter matches the site's category filter")
    public PageResponse<PublicVideo> list(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size,
                                          @RequestParam(required = false) String filter,
                                          @RequestParam(required = false) String category) {
        return videoService.publicList(category != null ? category : filter, page, size);
    }

    @GetMapping("/{slug}")
    @Operation(summary = "One published video by slug")
    public PublicVideo get(@PathVariable String slug) {
        return videoService.publicGet(slug);
    }
}
