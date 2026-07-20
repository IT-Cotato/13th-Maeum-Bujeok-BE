package com.maumbujeok.backend.domain.diary.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maumbujeok.backend.domain.diary.application.DiaryService;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryRequest;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryResponse;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.global.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DiaryControllerSecurityTest {
    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired DiaryService diaryService;
    @Autowired JwtTokenProvider jwtTokenProvider;

    @Test
    void rejectsUnauthenticatedDiaryCreation() throws Exception {
        mockMvc.perform(post("/api/diaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"오늘의 기록\",\"selectedEmotion\":\"불안\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedMemberCreatesDiaryWithPendingAnalysis() throws Exception {
        Member member = saveMember("owner", "01000000001");
        String token = jwtTokenProvider.createToken(member.getLoginId(), member.getRole().name());

        mockMvc.perform(post("/api/diaries")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"오늘은 불안했지만 잘 견뎠다\",\"selectedEmotion\":\"불안\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.analysisStatus").value("PENDING"));
    }

    @Test
    void hidesAnotherMembersAnalysis() throws Exception {
        Member owner = saveMember("owner2", "01000000002");
        Member stranger = saveMember("stranger", "01000000003");
        CreateDiaryResponse created = diaryService.create(owner.getId(), new CreateDiaryRequest("나만의 일기", "슬픔"));
        String strangerToken = jwtTokenProvider.createToken(stranger.getLoginId(), stranger.getRole().name());

        mockMvc.perform(get("/api/diaries/{diaryId}/analysis", created.diaryId())
                        .header("Authorization", "Bearer " + strangerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DIARY_404"));
    }

    @Test
    void returnsTemporaryAmuletGenerationFieldsWithAnalysis() throws Exception {
        Member member = saveMember("amulet", "01000000004");
        CreateDiaryResponse created = diaryService.create(
                member.getId(),
                new CreateDiaryRequest("친구와 즐거운 하루를 보냈다", "기쁨")
        );
        String token = jwtTokenProvider.createToken(member.getLoginId(), member.getRole().name());

        mockMvc.perform(get("/api/diaries/{diaryId}/analysis", created.diaryId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amuletId").value(0))
                .andExpect(jsonPath("$.data.amuletType").value("friendship"))
                .andExpect(jsonPath("$.data.title").value("테스트 응답"))
                .andExpect(jsonPath("$.data.createdAt").isString())
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty());
    }

    private Member saveMember(String loginId, String phoneNumber) {
        return memberRepository.save(Member.builder()
                .loginId(loginId)
                .phoneNumber(phoneNumber)
                .passwordHash("encoded-password")
                .role(Member.Role.ROLE_USER)
                .build());
    }
}
