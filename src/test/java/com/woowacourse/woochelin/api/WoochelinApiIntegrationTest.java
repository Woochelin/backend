package com.woowacourse.woochelin.api;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.woowacourse.woochelin.coach.Coach;
import com.woowacourse.woochelin.coach.CoachRepository;
import com.woowacourse.woochelin.coach.CoachReviewRepository;
import com.woowacourse.woochelin.common.SearchLogRepository;
import com.woowacourse.woochelin.common.TargetType;
import com.woowacourse.woochelin.reviewer.Reviewer;
import com.woowacourse.woochelin.reviewer.ReviewerRepository;
import com.woowacourse.woochelin.reviewer.ReviewerReviewRepository;
import com.woowacourse.woochelin.tag.Tag;
import com.woowacourse.woochelin.tag.TagRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "gemini.api-key="
})
class WoochelinApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CoachRepository coachRepository;

    @Autowired
    private ReviewerRepository reviewerRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private CoachReviewRepository coachReviewRepository;

    @Autowired
    private ReviewerReviewRepository reviewerReviewRepository;

    @Autowired
    private SearchLogRepository searchLogRepository;

    @BeforeEach
    void setUp() {
        searchLogRepository.deleteAll();
        coachReviewRepository.deleteAll();
        reviewerReviewRepository.deleteAll();
    }

    @Test
    @DisplayName("초기 seed 데이터로 코치 태그와 코치 목록을 조회한다")
    void getSeededTagsAndCoaches() throws Exception {
        mockMvc.perform(get("/api/v1/tags")
                        .queryParam("type", "COACH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(17)))
                .andExpect(jsonPath("$[*].name", hasItem("번아웃케어")));

        mockMvc.perform(get("/api/v1/coaches")
                        .queryParam("part", "backend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("검프")))
                .andExpect(jsonPath("$[*].part", hasItem("BACKEND")));
    }

    @Test
    @DisplayName("상세 조회는 조회 로그를 남기고 챗봇 급상승 및 홈 랭킹에 반영된다")
    void detailViewCreatesRankingLog() throws Exception {
        Coach gump = coachRepository.findByBotId("gump").orElseThrow();

        mockMvc.perform(get("/api/v1/coaches/{coachId}", gump.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("검프"));

        mockMvc.perform(get("/api/v1/chatbot/trending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].targetType").value("COACH"))
                .andExpect(jsonPath("$[0].targetId").value(gump.getId()))
                .andExpect(jsonPath("$[0].viewCount").value(1));

        mockMvc.perform(get("/api/v1/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weeklyRanking[0].name").value("검프"))
                .andExpect(jsonPath("$.weeklyCoachRanking[0].name").value("검프"));
    }

    @Test
    @DisplayName("코치 리뷰 작성 시 상위 태그는 3개만 노출되고 삭제 후 상세 조회에서 제외된다")
    void createAndSoftDeleteCoachReview() throws Exception {
        Coach gump = coachRepository.findByBotId("gump").orElseThrow();
        List<Long> tagIds = tagRepository.findByTargetTypeOrderById(TargetType.COACH).stream()
                .limit(4)
                .map(Tag::getId)
                .toList();

        MvcResult result = mockMvc.perform(post("/api/v1/coaches/{coachId}/reviews", gump.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "",
                                  "password": "1234",
                                  "rating": 5,
                                  "content": "질문이 날카로웠어요",
                                  "tagIds": [%d, %d, %d, %d]
                                }
                                """.formatted(tagIds.get(0), tagIds.get(1), tagIds.get(2), tagIds.get(3))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nickname").value("익명"))
                .andExpect(jsonPath("$.tags", hasSize(4)))
                .andReturn();

        Number reviewId = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/v1/coaches/{coachId}", gump.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(5.0))
                .andExpect(jsonPath("$.topTags", hasSize(3)))
                .andExpect(jsonPath("$.reviews", hasSize(1)));

        mockMvc.perform(delete("/api/v1/coaches/{coachId}/reviews/{reviewId}", gump.getId(), reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "1234"
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/coaches/{coachId}", gump.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(0.0))
                .andExpect(jsonPath("$.topTags", hasSize(0)))
                .andExpect(jsonPath("$.reviews", hasSize(0)));
    }

    @Test
    @DisplayName("코치 리뷰는 비밀번호 검증 후 수정할 수 있다")
    void updateCoachReview() throws Exception {
        Coach gump = coachRepository.findByBotId("gump").orElseThrow();
        List<Long> tagIds = tagRepository.findByTargetTypeOrderById(TargetType.COACH).stream()
                .limit(2)
                .map(Tag::getId)
                .toList();

        MvcResult result = mockMvc.perform(post("/api/v1/coaches/{coachId}/reviews", gump.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "크루",
                                  "password": "1234",
                                  "rating": 3,
                                  "content": "처음 내용",
                                  "tagIds": [%d]
                                }
                                """.formatted(tagIds.getFirst())))
                .andExpect(status().isCreated())
                .andReturn();
        Number reviewId = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(patch("/api/v1/coaches/{coachId}/reviews/{reviewId}", gump.getId(), reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "수정크루",
                                  "password": "1234",
                                  "rating": 5,
                                  "content": "수정된 내용",
                                  "tagIds": [%d, %d]
                                }
                                """.formatted(tagIds.get(0), tagIds.get(1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("수정크루"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.content").value("수정된 내용"))
                .andExpect(jsonPath("$.tags", hasSize(2)));
    }

    @Test
    @DisplayName("통합 검색은 코치와 리뷰어를 함께 반환한다")
    void searchAcrossCoachAndReviewer() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                        .queryParam("keyword", "검프"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keyword").value("검프"))
                .andExpect(jsonPath("$.results[0].targetType").value("COACH"))
                .andExpect(jsonPath("$.results[0].name").value("검프"));
    }

    @Test
    @DisplayName("리뷰어 리뷰에는 리뷰어 태그만 허용한다")
    void reviewerReviewRejectsCoachTag() throws Exception {
        Reviewer reviewer = reviewerRepository.findAll().getFirst();
        Long coachTagId = tagRepository.findByTargetTypeOrderById(TargetType.COACH).getFirst().getId();

        mockMvc.perform(post("/api/v1/reviewers/{reviewerId}/reviews", reviewer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "크루",
                                  "password": "1234",
                                  "rating": 4,
                                  "content": "꼼꼼했어요",
                                  "tagIds": [%d]
                                }
                                """.formatted(coachTagId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("리뷰어 리뷰에 사용할 수 없는 태그가 포함되어 있습니다."));
    }

    @Test
    @DisplayName("Gemini API 키가 없으면 챗봇 API는 명확한 503 응답을 반환한다")
    void chatbotReturnsServiceUnavailableWithoutApiKey() throws Exception {
        mockMvc.perform(post("/api/v1/chatbot/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sessionId": "11111111-1111-1111-1111-111111111111",
                                  "botId": "gump",
                                  "crewName": "체체",
                                  "message": "코딩이 어려워요"
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Gemini API 키가 설정되어 있지 않습니다. gemini.api-key를 설정해 주세요."));
    }

    @Test
    @DisplayName("별점 범위를 벗어난 리뷰 요청은 검증 단계에서 거절한다")
    void rejectInvalidRating() throws Exception {
        Coach gump = coachRepository.findByBotId("gump").orElseThrow();

        mockMvc.perform(post("/api/v1/coaches/{coachId}/reviews", gump.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "크루",
                                  "password": "1234",
                                  "rating": 6,
                                  "content": "좋았어요",
                                  "tagIds": []
                                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", startsWith("rating:")));
    }
}
