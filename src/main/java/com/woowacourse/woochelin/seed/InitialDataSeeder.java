package com.woowacourse.woochelin.seed;

import com.woowacourse.woochelin.coach.Coach;
import com.woowacourse.woochelin.coach.CoachRepository;
import com.woowacourse.woochelin.coach.CoachReview;
import com.woowacourse.woochelin.coach.CoachReviewRepository;
import com.woowacourse.woochelin.common.ActivityLog;
import com.woowacourse.woochelin.common.ActivityLogRepository;
import com.woowacourse.woochelin.common.Part;
import com.woowacourse.woochelin.common.SearchKeywordLog;
import com.woowacourse.woochelin.common.SearchKeywordLogRepository;
import com.woowacourse.woochelin.common.SearchLog;
import com.woowacourse.woochelin.common.SearchLogRepository;
import com.woowacourse.woochelin.common.TargetType;
import com.woowacourse.woochelin.reviewer.Reviewer;
import com.woowacourse.woochelin.reviewer.ReviewerRepository;
import com.woowacourse.woochelin.reviewer.ReviewerReview;
import com.woowacourse.woochelin.reviewer.ReviewerReviewRepository;
import com.woowacourse.woochelin.tag.Tag;
import com.woowacourse.woochelin.tag.TagRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class InitialDataSeeder implements ApplicationRunner {

    private static final String[] COACH_TAGS = {
            "하드스킬", "소프트스킬", "성장", "진로상담", "포트폴리오", "매운맛", "순한맛", "취업", "직설적",
            "부드러운", "프로젝트 정리", "장기적관점", "목표 설정", "격려 위주", "공감능력", "번아웃케어", "회고 도움"
    };

    private static final String[] REVIEWER_TAGS = {
            "하드스킬", "리뷰 늦음", "리뷰 빠름", "칼답", "응답느림", "해외직장", "칭찬요정", "객체지향",
            "ai친화적", "질문 많음", "꼼꼼함", "간결함", "사고유도", "커피챗 호", "커피챗 불호", "따뜻한",
            "차가운", "머지 당함", "머지 안해줌"
    };

    private static final Map<String, String> BOT_IDS = Map.ofEntries(
            Map.entry("검프", "gump"),
            Map.entry("구구", "gugu"),
            Map.entry("네오", "neo"),
            Map.entry("브라운", "brown"),
            Map.entry("브리", "brie"),
            Map.entry("시지프", "sisyphus"),
            Map.entry("준", "jun"),
            Map.entry("디노", "dino"),
            Map.entry("레아", "lea"),
            Map.entry("제임스", "james"),
            Map.entry("제이슨", "jason"),
            Map.entry("포비", "pobi"),
            Map.entry("류시", "ryusi"),
            Map.entry("리사", "lisa"),
            Map.entry("왼손", "left-hand"),
            Map.entry("워니", "woni")
    );

    private static final Map<String, String> BOT_DESCRIPTIONS = Map.ofEntries(
            Map.entry("검프", "따뜻한 위로와 명언"),
            Map.entry("구구", "유쾌한 백엔드 지식"),
            Map.entry("네오", "번아웃 해결사"),
            Map.entry("브라운", "정확한 기술 피드백"),
            Map.entry("브리", "장기적 성장 관점"),
            Map.entry("디노", "안드로이드 성장 코칭"),
            Map.entry("레아", "부드러운 진로 상담"),
            Map.entry("제이슨", "하드스킬 집중 코칭"),
            Map.entry("포비", "우테코 성장 철학"),
            Map.entry("리사", "소프트스킬 정리")
    );

    private static final Map<String, String> REVIEWER_STYLES = Map.ofEntries(
            Map.entry("로빈", "따뜻함"),
            Map.entry("피케이", "매운맛"),
            Map.entry("제이미", "칼답"),
            Map.entry("수야", "순한맛"),
            Map.entry("우디", "꼼꼼함"),
            Map.entry("두루", "따뜻함"),
            Map.entry("기론", "간결함"),
            Map.entry("백호", "사고유도"),
            Map.entry("아루", "매운맛"),
            Map.entry("썬", "칭찬요정")
    );

    private final TagRepository tagRepository;
    private final CoachRepository coachRepository;
    private final ReviewerRepository reviewerRepository;
    private final CoachReviewRepository coachReviewRepository;
    private final ReviewerReviewRepository reviewerReviewRepository;
    private final SearchLogRepository searchLogRepository;
    private final SearchKeywordLogRepository searchKeywordLogRepository;
    private final ActivityLogRepository activityLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResourcePatternResolver resourcePatternResolver;

    public InitialDataSeeder(
            TagRepository tagRepository,
            CoachRepository coachRepository,
            ReviewerRepository reviewerRepository,
            CoachReviewRepository coachReviewRepository,
            ReviewerReviewRepository reviewerReviewRepository,
            SearchLogRepository searchLogRepository,
            SearchKeywordLogRepository searchKeywordLogRepository,
            ActivityLogRepository activityLogRepository,
            PasswordEncoder passwordEncoder,
            ResourcePatternResolver resourcePatternResolver
    ) {
        this.tagRepository = tagRepository;
        this.coachRepository = coachRepository;
        this.reviewerRepository = reviewerRepository;
        this.coachReviewRepository = coachReviewRepository;
        this.reviewerReviewRepository = reviewerReviewRepository;
        this.searchLogRepository = searchLogRepository;
        this.searchKeywordLogRepository = searchKeywordLogRepository;
        this.activityLogRepository = activityLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        seedTags(TargetType.COACH, COACH_TAGS);
        seedTags(TargetType.REVIEWER, REVIEWER_TAGS);
        seedCoaches();
        seedReviewers();
        seedReviews();
        seedSearchLogs();
        seedTrendingSearchTerms();
        seedActivityLogs();
    }

    private void seedTags(TargetType targetType, String[] names) {
        for (String name : names) {
            if (!tagRepository.existsByTargetTypeAndName(targetType, name)) {
                tagRepository.save(new Tag(targetType, name));
            }
        }
    }

    private void seedCoaches() throws IOException {
        Map<String, String> personas = loadPersonas();
        for (Resource resource : resourcePatternResolver.getResources("classpath:/static/coach/*")) {
            String filename = resource.getFilename();
            if (!isImage(filename)) {
                continue;
            }
            ProfileFile profile = ProfileFile.from(filename);
            if (coachRepository.existsByName(profile.name())) {
                continue;
            }
            String botId = BOT_IDS.getOrDefault(profile.name(), profile.name());
            String persona = personas.getOrDefault(profile.name(), defaultPersona(profile.name()));
            String botDescription = BOT_DESCRIPTIONS.getOrDefault(profile.name(), "따뜻한 위로와 명언");
            coachRepository.save(new Coach(
                    profile.name(),
                    profile.part(),
                    "/coach/" + filename,
                    "https://woowacourse.slack.com/team/" + botId,
                    botId,
                    botDescription,
                    persona
            ));
        }
    }

    private void seedReviewers() throws IOException {
        for (Resource resource : resourcePatternResolver.getResources("classpath:/static/reviewer/*")) {
            String filename = resource.getFilename();
            if (!isImage(filename)) {
                continue;
            }
            ProfileFile profile = ProfileFile.from(filename);
            if (reviewerRepository.existsByName(profile.name())) {
                continue;
            }
            String style = REVIEWER_STYLES.getOrDefault(profile.name(), "따뜻함");
            reviewerRepository.save(new Reviewer(
                    profile.name(),
                    profile.part(),
                    "/reviewer/" + filename,
                    "https://woowacourse.slack.com",
                    style
            ));
        }
    }

    private void seedReviews() {
        if (coachReviewRepository.count() > 0 || reviewerReviewRepository.count() > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        String password = passwordEncoder.encode("1234");

        seedCoachReview("검프", "익명의크루", password, 5, "원온원 때 정말 큰 위로를 받았습니다. 코드 리뷰도 너무 꼼꼼하게 봐주셔요!", List.of("성장", "프로젝트 정리"), now.minusMinutes(1));
        seedCoachReview("검프", "스프링러버", password, 5, "하드스킬 적으로 성장할 수 있는 인사이트를 많이 주셨습니다.", List.of("하드스킬", "직설적"), now.minusMinutes(18));
        seedCoachReview("구구", "자바칩", password, 5, "막힌 지점을 유쾌하게 풀어주셔서 다시 시도할 힘이 생겼습니다.", List.of("진로상담", "순한맛"), now.minusMinutes(34));
        seedCoachReview("네오", "디자인패턴충", password, 5, "번아웃 상태였는데 방향을 차분하게 정리해주셨어요.", List.of("번아웃케어", "공감능력"), now.minusHours(1));
        seedCoachReview("브라운", "백엔드러버", password, 5, "질문이 날카롭고 구현의 빈틈을 정확히 짚어주십니다.", List.of("매운맛", "하드스킬"), now.minusHours(2));
        seedCoachReview("리사", "타입스크립트", password, 4, "소프트스킬과 커뮤니케이션 방향을 구체적으로 잡아주셨어요.", List.of("소프트스킬", "부드러운"), now.minusHours(3));
        seedCoachReview("디노", "안드장인", password, 4, "목표 설정을 작게 쪼개는 데 큰 도움을 받았습니다.", List.of("목표 설정", "격려 위주"), now.minusHours(4));

        seedReviewerReview("로빈", "스프링러버", password, 5, "리뷰가 따뜻하면서도 놓친 부분을 정확히 알려줘서 좋았습니다.", List.of("칭찬요정", "꼼꼼함", "따뜻한"), now.minusMinutes(2));
        seedReviewerReview("피케이", "테코충", password, 5, "빠르고 명확합니다. 조금 매운맛이지만 남는 게 많았어요.", List.of("칼답", "객체지향", "차가운"), now.minusMinutes(12));
        seedReviewerReview("기론", "우테코가자", password, 4, "리뷰가 간결해서 바로 액션으로 옮기기 좋았습니다.", List.of("간결함", "리뷰 빠름"), now.minusMinutes(45));
        seedReviewerReview("백호", "맥북오너", password, 4, "정답을 알려주기보다 스스로 생각하게 만드는 질문이 좋았습니다.", List.of("사고유도", "꼼꼼함"), now.minusHours(2));
        seedReviewerReview("수야", "프론트요정", password, 5, "칭찬과 개선 포인트의 균형이 좋아서 부담 없이 반영했습니다.", List.of("따뜻한", "칭찬요정"), now.minusHours(5));
    }

    private void seedCoachReview(
            String coachName,
            String nickname,
            String password,
            int rating,
            String content,
            List<String> tagNames,
            LocalDateTime createdAt
    ) {
        coachRepository.findByName(coachName).ifPresent(coach -> coachReviewRepository.save(new CoachReview(
                coach,
                nickname,
                password,
                rating,
                content,
                tags(TargetType.COACH, tagNames),
                createdAt
        )));
    }

    private void seedReviewerReview(
            String reviewerName,
            String nickname,
            String password,
            int rating,
            String content,
            List<String> tagNames,
            LocalDateTime createdAt
    ) {
        reviewerRepository.findByName(reviewerName).ifPresent(reviewer -> reviewerReviewRepository.save(new ReviewerReview(
                reviewer,
                nickname,
                password,
                rating,
                content,
                tags(TargetType.REVIEWER, tagNames),
                createdAt
        )));
    }

    private Set<Tag> tags(TargetType targetType, List<String> names) {
        Set<Tag> tags = new LinkedHashSet<>();
        for (Tag tag : tagRepository.findByTargetTypeOrderById(targetType)) {
            if (names.contains(tag.getName())) {
                tags.add(tag);
            }
        }
        return tags;
    }

    private void seedSearchLogs() {
        if (searchLogRepository.count() > 0) {
            return;
        }
        seedSearchLog("검프", TargetType.COACH, "검프 피드백", 14);
        seedSearchLog("리사", TargetType.COACH, "리사 꼼꼼함", 11);
        seedSearchLog("로빈", TargetType.REVIEWER, "로빈 리뷰어", 10);
        seedSearchLog("디노", TargetType.COACH, "AOS 디노", 8);
        seedSearchLog("피케이", TargetType.REVIEWER, "피케이 프론트", 7);
        seedSearchLog("구구", TargetType.COACH, "원온원 꿀팁", 6);
        seedSearchLog("수야", TargetType.REVIEWER, "리액트 렌더링", 5);
    }

    private void seedSearchLog(String targetName, TargetType targetType, String keyword, int count) {
        Long targetId = targetType == TargetType.COACH
                ? coachRepository.findByName(targetName).map(Coach::getId).orElse(null)
                : reviewerRepository.findByName(targetName).map(Reviewer::getId).orElse(null);
        if (targetId == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (int index = 0; index < count; index++) {
            searchLogRepository.save(new SearchLog(keyword, targetType, targetId, now.minusMinutes(index * 7L)));
        }
    }

    private void seedTrendingSearchTerms() {
        if (searchKeywordLogRepository.count() > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        searchKeywordLogRepository.save(new SearchKeywordLog("검프 피드백", "NEW", "new", 1204, now));
        searchKeywordLogRepository.save(new SearchKeywordLog("리사 꼼꼼함", "UP", "up", 982, now));
        searchKeywordLogRepository.save(new SearchKeywordLog("로빈 리뷰어", "DOWN", "down", 856, now));
        searchKeywordLogRepository.save(new SearchKeywordLog("AOS 디노", null, "", 720, now));
        searchKeywordLogRepository.save(new SearchKeywordLog("피케이 프론트", "NEW", "new", 650, now));
        searchKeywordLogRepository.save(new SearchKeywordLog("원온원 꿀팁", "UP", "up", 412, now));
        searchKeywordLogRepository.save(new SearchKeywordLog("리액트 렌더링", null, "", 380, now));
    }

    private void seedActivityLogs() {
        if (activityLogRepository.count() > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        activityLogRepository.save(new ActivityLog("review", "#3DBBBD", "익명의크루", "님이", "검프 코치", "에게 리뷰를 남겼습니다.", "★★★★★", null, now.minusSeconds(20)));
        activityLogRepository.save(new ActivityLog("tag", "#f5a623", "스프링러버", "님이", "로빈 리뷰어", "의 태그를 추가했습니다.", "#따뜻함", "tag", now.minusMinutes(2)));
        activityLogRepository.save(new ActivityLog("oneonone", "#9b59b6", "프론트요정", "님이", "제이슨 코치", "에게 원온원을 신청했습니다.", null, null, now.minusMinutes(5)));
        activityLogRepository.save(new ActivityLog("review", "#3DBBBD", "테코충", "님이", "피케이 리뷰어", "에게 리뷰를 남겼습니다.", "★★★★☆", null, now.minusMinutes(12)));
        activityLogRepository.save(new ActivityLog("tag", "#f5a623", "자바칩", "님이", "구구 코치", "의 태그를 추가했습니다.", "#진로상담", "tag", now.minusMinutes(15)));
        activityLogRepository.save(new ActivityLog("review", "#3DBBBD", "디자인패턴충", "님이", "네오 코치", "에게 리뷰를 남겼습니다.", "★★★★★", null, now.minusMinutes(21)));
        activityLogRepository.save(new ActivityLog("oneonone", "#9b59b6", "맥북오너", "님이", "워니 코치", "에게 원온원을 신청했습니다.", null, null, now.minusMinutes(30)));
        activityLogRepository.save(new ActivityLog("review", "#3DBBBD", "안드장인", "님이", "디노 코치", "에게 리뷰를 남겼습니다.", "★★★★☆", null, now.minusMinutes(45)));
        activityLogRepository.save(new ActivityLog("tag", "#f5a623", "타입스크립트", "님이", "리사 코치", "의 태그를 추가했습니다.", "#소프트스킬", "tag", now.minusHours(1)));
        activityLogRepository.save(new ActivityLog("coffeechat", "#9b59b6", "우테코가자", "님이", "포비 코치", "에게 커피챗을 신청했습니다.", null, null, now.minusHours(2)));
    }

    private Map<String, String> loadPersonas() throws IOException {
        java.util.Map<String, String> personas = new java.util.HashMap<>();
        for (Resource resource : resourcePatternResolver.getResources("classpath:/static/*_페르소나.md")) {
            String filename = Normalizer.normalize(resource.getFilename(), Normalizer.Form.NFC);
            String name = filename.substring(0, filename.indexOf("_페르소나"));
            String prompt = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            personas.put(name, prompt);
        }
        return personas;
    }

    private boolean isImage(String filename) {
        if (filename == null) {
            return false;
        }
        String lower = filename.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpeg") || lower.endsWith(".jpg") || lower.endsWith(".webp");
    }

    private String defaultPersona(String name) {
        return "너는 우아한테크코스의 '" + name + "' 코치야. 항상 존댓말을 사용하고, "
                + "사용자가 스스로 생각을 정리할 수 있도록 짧은 질문과 구체적인 다음 행동을 제안해. "
                + "정답을 단정하기보다 코치의 관점에서 조심스럽게 의견을 전달해.";
    }

    private record ProfileFile(Part part, String name) {

        static ProfileFile from(String filename) {
            String normalized = Normalizer.normalize(filename, Normalizer.Form.NFC);
            String withoutExtension = normalized.substring(0, normalized.lastIndexOf('.'));
            String[] tokens = withoutExtension.split("_");
            String partCode = tokens[0];
            String name = tokens[tokens.length - 1];
            return new ProfileFile(Part.from(partCode), name);
        }
    }
}
