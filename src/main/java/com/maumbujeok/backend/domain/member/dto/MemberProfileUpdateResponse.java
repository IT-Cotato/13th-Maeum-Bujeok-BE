package com.maumbujeok.backend.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 프로필 수정 결과")
public record MemberProfileUpdateResponse(
        @Schema(description = "수정된 회원 프로필") MemberProfileResponse profile,
        @Schema(description = "로컬 계정에서 전화번호가 바뀌어 재인증이 필요한지 여부", example = "true")
        boolean reauthenticationRequired
) {
    public static MemberProfileUpdateResponse of(MemberProfileResponse profile, boolean reauthenticationRequired) {
        return new MemberProfileUpdateResponse(profile, reauthenticationRequired);
    }
}
