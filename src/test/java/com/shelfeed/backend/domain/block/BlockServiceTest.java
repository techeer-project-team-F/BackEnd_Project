package com.shelfeed.backend.domain.block;

import com.shelfeed.backend.domain.block.dto.response.BlockListResponse;
import com.shelfeed.backend.domain.block.entity.Block;
import com.shelfeed.backend.domain.block.repository.BlockRepository;
import com.shelfeed.backend.domain.block.service.BlockService;
import com.shelfeed.backend.domain.feed.repository.FeedRepository;
import com.shelfeed.backend.domain.follow.entity.Follow;
import com.shelfeed.backend.domain.follow.repository.FollowRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BlockService 단위 테스트")
class BlockServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock BlockRepository blockRepository;
    @Mock FollowRepository followRepository;
    @Mock FeedRepository feedRepository;

    @InjectMocks BlockService blockService;

    Member blocker;
    Member blocked;
    Block block;

    @BeforeEach
    void setUp() {
        blocker = Member.createLocal(1L, "blocker@test.com", "encoded", "blocker", "bio");
        blocked = Member.createLocal(2L, "blocked@test.com", "encoded", "blocked", "bio");
        block = Block.create(blocker, blocked);
        ReflectionTestUtils.setField(block, "blockId", 10L);
    }

    @Nested
    @DisplayName("차단")
    class BlockUser {

        @Test
        @DisplayName("성공 - 팔로우 관계 있을 때 함께 해제")
        void 성공_팔로우_관계_해제() {
            Follow follow1 = mock(Follow.class); // blocker → blocked
            Follow follow2 = mock(Follow.class); // blocked → blocker

            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(blocker));
            given(memberRepository.findByMemberUserId(2L)).willReturn(Optional.of(blocked));
            given(blockRepository.existsByBlockerAndBlocked(blocker, blocked)).willReturn(false);
            given(followRepository.findByFollowerAndFollowee(blocker, blocked)).willReturn(Optional.of(follow1));
            given(followRepository.findByFollowerAndFollowee(blocked, blocker)).willReturn(Optional.of(follow2));

            blockService.block(2L, 1L);

            then(blockRepository).should().save(any(Block.class));
            then(followRepository).should().delete(follow1);
            then(memberRepository).should().decreaseFollowingCount(1L);
            then(memberRepository).should().decreaseFollowerCount(2L);
            then(followRepository).should().delete(follow2);
            then(memberRepository).should().decreaseFollowingCount(2L);
            then(memberRepository).should().decreaseFollowerCount(1L);
            then(feedRepository).should().deleteByMemberAndReview_Member(blocker, blocked);
        }

        @Test
        @DisplayName("성공 - 팔로우 관계 없을 때 팔로우 삭제 생략")
        void 성공_팔로우_관계_없음() {
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(blocker));
            given(memberRepository.findByMemberUserId(2L)).willReturn(Optional.of(blocked));
            given(blockRepository.existsByBlockerAndBlocked(blocker, blocked)).willReturn(false);
            given(followRepository.findByFollowerAndFollowee(blocker, blocked)).willReturn(Optional.empty());
            given(followRepository.findByFollowerAndFollowee(blocked, blocker)).willReturn(Optional.empty());

            blockService.block(2L, 1L);

            then(blockRepository).should().save(any(Block.class));
            then(followRepository).should(never()).delete(any(Follow.class));
            then(feedRepository).should().deleteByMemberAndReview_Member(blocker, blocked);
        }

        @Test
        @DisplayName("SELF_BLOCK_NOT_ALLOWED 예외")
        void 자기_자신_차단_예외() {
            assertThatThrownBy(() -> blockService.block(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SELF_BLOCK_NOT_ALLOWED);
        }

        @Test
        @DisplayName("MEMBER_NOT_FOUND 예외")
        void 멤버_없음_예외() {
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> blockService.block(2L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("ALREADY_BLOCKED 예외")
        void 이미_차단됨_예외() {
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(blocker));
            given(memberRepository.findByMemberUserId(2L)).willReturn(Optional.of(blocked));
            given(blockRepository.existsByBlockerAndBlocked(blocker, blocked)).willReturn(true);

            assertThatThrownBy(() -> blockService.block(2L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_BLOCKED);
        }
    }

    @Nested
    @DisplayName("차단 해제")
    class UnblockUser {

        @Test
        @DisplayName("성공")
        void 성공() {
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(blocker));
            given(memberRepository.findByMemberUserId(2L)).willReturn(Optional.of(blocked));
            given(blockRepository.findByBlockerAndBlocked(blocker, blocked)).willReturn(Optional.of(block));

            blockService.unblock(2L, 1L);

            then(blockRepository).should().delete(block);
        }

        @Test
        @DisplayName("MEMBER_NOT_FOUND 예외")
        void 멤버_없음_예외() {
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> blockService.unblock(2L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("BLOCK_NOT_FOUND 예외")
        void 차단_관계_없음_예외() {
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(blocker));
            given(memberRepository.findByMemberUserId(2L)).willReturn(Optional.of(blocked));
            given(blockRepository.findByBlockerAndBlocked(blocker, blocked)).willReturn(Optional.empty());

            assertThatThrownBy(() -> blockService.unblock(2L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BLOCK_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("차단 목록 조회")
    class GetBlockList {

        @Test
        @DisplayName("성공")
        void 성공() {
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(blocker));
            given(blockRepository.findBlocks(eq(blocker), isNull(), any(PageRequest.class))).willReturn(List.of(block));

            BlockListResponse response = blockService.getBlockList(1L, null, 10);

            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getUserId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("MEMBER_NOT_FOUND 예외")
        void 멤버_없음_예외() {
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> blockService.getBlockList(1L, null, 10))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
        }
    }
}
