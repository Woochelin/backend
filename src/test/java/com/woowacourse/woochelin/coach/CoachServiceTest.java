package com.woowacourse.woochelin.coach;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.woowacourse.woochelin.common.Part;
import com.woowacourse.woochelin.common.ReviewRequest;
import com.woowacourse.woochelin.common.SearchLogRepository;
import com.woowacourse.woochelin.common.TargetType;
import com.woowacourse.woochelin.common.exception.BadRequestException;
import com.woowacourse.woochelin.tag.Tag;
import com.woowacourse.woochelin.tag.TagRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CoachServiceTest {

    @Mock
    private CoachRepository coachRepository;

    @Mock
    private CoachReviewRepository coachReviewRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private SearchLogRepository searchLogRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CoachService coachService;

    @Test
    @DisplayName("코치 리뷰 작성 시 닉네임 기본값과 해싱된 비밀번호를 저장한다")
    void createReviewWithDefaultNicknameAndEncodedPassword() {
        Coach coach = new Coach("검프", Part.BACKEND, "/coach/gump.png", "https://slack", "gump", "prompt");
        Tag tag = new Tag(TargetType.COACH, "매운맛");
        ReviewRequest request = new ReviewRequest("", "raw-password", 5, "날카로웠어요", List.of(1L, 1L));
        when(coachRepository.findById(1L)).thenReturn(Optional.of(coach));
        when(tagRepository.findByIdInAndTargetType(List.of(1L), TargetType.COACH)).thenReturn(List.of(tag));
        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");
        when(coachReviewRepository.save(any(CoachReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        coachService.createReview(1L, request);

        ArgumentCaptor<CoachReview> reviewCaptor = ArgumentCaptor.forClass(CoachReview.class);
        verify(coachReviewRepository).save(reviewCaptor.capture());
        CoachReview savedReview = reviewCaptor.getValue();
        assertThat(savedReview.getNickname()).isEqualTo("익명");
        assertThat(savedReview.getPassword()).isEqualTo("encoded-password");
        assertThat(savedReview.getTags()).containsExactly(tag);
    }

    @Test
    @DisplayName("코치 리뷰에는 코치 태그만 사용할 수 있다")
    void rejectInvalidCoachTags() {
        Coach coach = new Coach("검프", Part.BACKEND, "/coach/gump.png", "https://slack", "gump", "prompt");
        ReviewRequest request = new ReviewRequest("크루", "1234", 5, "좋았어요", List.of(1L, 2L));
        when(coachRepository.findById(1L)).thenReturn(Optional.of(coach));
        when(tagRepository.findByIdInAndTargetType(List.of(1L, 2L), TargetType.COACH)).thenReturn(List.of(new Tag(TargetType.COACH, "매운맛")));

        assertThatThrownBy(() -> coachService.createReview(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("태그");
    }
}
