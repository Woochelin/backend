package com.woowacourse.woochelin.seed;

import com.woowacourse.woochelin.coach.Coach;
import com.woowacourse.woochelin.coach.CoachRepository;
import com.woowacourse.woochelin.common.Part;
import com.woowacourse.woochelin.common.TargetType;
import com.woowacourse.woochelin.reviewer.Reviewer;
import com.woowacourse.woochelin.reviewer.ReviewerRepository;
import com.woowacourse.woochelin.tag.Tag;
import com.woowacourse.woochelin.tag.TagRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Map;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
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

    private final TagRepository tagRepository;
    private final CoachRepository coachRepository;
    private final ReviewerRepository reviewerRepository;
    private final ResourcePatternResolver resourcePatternResolver;

    public InitialDataSeeder(
            TagRepository tagRepository,
            CoachRepository coachRepository,
            ReviewerRepository reviewerRepository,
            ResourcePatternResolver resourcePatternResolver
    ) {
        this.tagRepository = tagRepository;
        this.coachRepository = coachRepository;
        this.reviewerRepository = reviewerRepository;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        seedTags(TargetType.COACH, COACH_TAGS);
        seedTags(TargetType.REVIEWER, REVIEWER_TAGS);
        seedCoaches();
        seedReviewers();
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
            coachRepository.save(new Coach(
                    profile.name(),
                    profile.part(),
                    "/coach/" + filename,
                    "https://woowacourse.slack.com/team/" + botId,
                    botId,
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
            reviewerRepository.save(new Reviewer(
                    profile.name(),
                    profile.part(),
                    "/reviewer/" + filename,
                    "https://woowacourse.slack.com"
            ));
        }
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
