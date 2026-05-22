package com.woowacourse.woochelin.tag;

import com.woowacourse.woochelin.common.TargetType;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TagController {

    private final TagRepository tagRepository;

    public TagController(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @GetMapping("/api/v1/tags")
    public List<TagResponse> tags(@RequestParam("type") TargetType type) {
        return tagRepository.findByTargetTypeOrderById(type).stream()
                .map(TagResponse::from)
                .toList();
    }
}
