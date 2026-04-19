package com.shelfeed.backend.domain.follow;

import com.shelfeed.backend.domain.feed.repository.FeedRepository;
import com.shelfeed.backend.domain.follow.dto.response.FollowResponse;
import com.shelfeed.backend.domain.follow.repository.FollowRepository;
import com.shelfeed.backend.domain.follow.service.FollowService;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.global.common.exception.BusinessException;
import com.shelfeed.backend.global.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowService 단위 테스트")
class FollowServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock FollowRepository followRepository;
    @Mock FeedRepository feedRepository;

    @InjectMocks FollowService followService;

    private Member follower;
    private Member followee;

    @BeforeEach
    void setUp() {
        // Member.createLocal(memberUserId, email, password, nickname, bio)
        follower = Member.createLocal(1L, "follower@test.com", "encoded", "팔로워닉네임", "bio");
        followee = Member.createLocal(2L, "followee@test.com", "encoded", "팔로위닉네임", "bio");
    }

    // ────────────────────────────────────────────────────────
    // follow()
    // ────────────────────────────────────────────────────────
    @Nested
    @DisplayName("팔로우")
    class Follow {

        @Test
        @DisplayName("자기 자신을 팔로우하면 CANNOT_FOLLOW_SELF 예외가 발생한다")
        void 자기자신_팔로우_예외() {
            assertThatThrownBy(() -> followService.follow(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CANNOT_FOLLOW_SELF);
        }

        @Test
        @DisplayName("존재하지 않는 회원을 팔로우하면 MEMBER_NOT_FOUND 예외가 발생한다")
        void 존재하지않는_회원_팔로우_예외() {
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(follower));
            given(memberRepository.findByMemberUserId(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> followService.follow(99L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("이미 팔로우한 사용자를 다시 팔로우하면 ALREADY_FOLLOWING 예외가 발생한다")
        void 중복_팔로우_예외() {
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(follower));
            given(memberRepository.findByMemberUserId(2L)).willReturn(Optional.of(followee));
            given(followRepository.existsByFollowerAndFollowee(follower, followee)).willReturn(true);

            assertThatThrownBy(() -> followService.follow(2L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.ALREADY_FOLLOWING);
        }

        @Test
        @DisplayName("정상 팔로우 시 FollowResponse를 반환하고 카운트가 증가한다")
        void 정상_팔로우_성공() {
            com.shelfeed.backend.domain.follow.entity.Follow follow =
                    com.shelfeed.backend.domain.follow.entity.Follow.create(follower, followee);

            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(follower));
            given(memberRepository.findByMemberUserId(2L)).willReturn(Optional.of(followee));
            given(followRepository.existsByFollowerAndFollowee(follower, followee)).willReturn(false);
            given(followRepository.save(any())).willReturn(follow);

            FollowResponse response = followService.follow(2L, 1L);

            assertThat(response.getFollowingUserId()).isEqualTo(2L);
            verify(memberRepository).increaseFollowingCount(1L); // 내 팔로잉 +1
            verify(memberRepository).increaseFollowerCount(2L);  // 상대 팔로워 +1
        }
    }

    // ────────────────────────────────────────────────────────
    // unfollow()
    // ────────────────────────────────────────────────────────
    @Nested
    @DisplayName("언팔로우")
    class Unfollow {

        @Test
        @DisplayName("팔로우하지 않은 대상을 언팔로우하면 FOLLOW_NOT_FOUND 예외가 발생한다")
        void 팔로우_내역없음_예외() {
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(follower));
            given(memberRepository.findByMemberUserId(2L)).willReturn(Optional.of(followee));
            given(followRepository.findByFollowerAndFollowee(follower, followee))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> followService.unfollow(2L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.FOLLOW_NOT_FOUND);
        }

        @Test
        @DisplayName("정상 언팔로우 시 Follow가 삭제되고 카운트가 감소한다")
        void 정상_언팔로우_성공() {
            com.shelfeed.backend.domain.follow.entity.Follow follow =
                    com.shelfeed.backend.domain.follow.entity.Follow.create(follower, followee);

            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(follower));
            given(memberRepository.findByMemberUserId(2L)).willReturn(Optional.of(followee));
            given(followRepository.findByFollowerAndFollowee(follower, followee))
                    .willReturn(Optional.of(follow));

            followService.unfollow(2L, 1L);

            verify(followRepository).delete(follow);
            verify(memberRepository).decreaseFollowingCount(1L); // 내 팔로잉 -1
            verify(memberRepository).decreaseFollowerCount(2L);  // 상대 팔로워 -1
        }

        @Test
        @DisplayName("언팔로우 시 팔로위의 피드에서 팔로워의 게시물이 제거된다")
        void 언팔로우_피드_제거() {
            com.shelfeed.backend.domain.follow.entity.Follow follow =
                    com.shelfeed.backend.domain.follow.entity.Follow.create(follower, followee);

            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(follower));
            given(memberRepository.findByMemberUserId(2L)).willReturn(Optional.of(followee));
            given(followRepository.findByFollowerAndFollowee(follower, followee))
                    .willReturn(Optional.of(follow));

            followService.unfollow(2L, 1L);

            // 언팔한 사람(follower)의 피드에서 팔로위(followee) 게시물 제거
            verify(feedRepository).deleteByMemberAndReview_Member(follower, followee);
        }

        @Test
        @DisplayName("팔로우 내역이 없으면 피드 삭제가 호출되지 않는다")
        void 팔로우_내역없으면_피드삭제_미호출() {
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(follower));
            given(memberRepository.findByMemberUserId(2L)).willReturn(Optional.of(followee));
            given(followRepository.findByFollowerAndFollowee(follower, followee))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> followService.unfollow(2L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.FOLLOW_NOT_FOUND);

            verify(feedRepository, never()).deleteByMemberAndReview_Member(any(), any());
        }
    }
}
