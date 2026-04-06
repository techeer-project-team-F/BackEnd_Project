package com.shelfeed.backend.domain.member.service;

import com.shelfeed.backend.domain.member.dto.request.UpdateProfileRequest;
import com.shelfeed.backend.domain.member.dto.response.MyProfileResponse;
import com.shelfeed.backend.domain.member.dto.response.UpdateProfileResponse;
import com.shelfeed.backend.domain.member.dto.response.UserProfileResponse;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.enums.MemberStatus;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.global.common.exception.BusinessException;
import com.shelfeed.backend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    // ── 2. 내 프로필 조회
    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(Long memberUserId){
        Member member = memberRepository.findByMemberUserId(memberUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return MyProfileResponse.of(member);
    }
    // ── 3. 프로필 수정
    public UpdateProfileResponse updateProfile(Long memberUserId, UpdateProfileRequest request){
        Member member = memberRepository.findByMemberUserId(memberUserId)
                .orElseThrow(()-> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        if (request.getNickname() != null && !request.getNickname().equals(member.getNickname())&& memberRepository.existsByNickname(request.getNickname())){//빈 값이 아닌지 현재 사용 중인지, 남이 사용중인 지
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        member.updateProfile(request.getNickname(), request.getBio(), request.getProfileImageUrl(), request.getLibraryVisibility());

        return UpdateProfileResponse.of(member);
    }
    // ── 4. 타 유저 프로필 조회
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long targetUserId){
        Member member = memberRepository.findByMemberUserId(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.WITHDRAWN_MEMBER);
        }
        return UserProfileResponse.of(member);
    }


}

