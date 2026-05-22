package com.woowacourse.woochelin.common;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 홈 화면 "최신 기록" 피드 API.
 * 최근 리뷰 작성 이력 최대 20건을 반환한다.
 */
@RestController
public class ActivityLogController {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogController(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @GetMapping("/api/v1/activity-logs")
    public List<ActivityLogResponse> recentLogs() {
        return activityLogRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(ActivityLogResponse::from)
                .toList();
    }
}
