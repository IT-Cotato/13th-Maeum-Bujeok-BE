package com.maumbujeok.backend.domain.diary.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.hasSize;

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
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(post("/api/diaries")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"오늘은 불안했지만 잘 견뎠다\",\"selectedEmotion\":\"ANXIOUS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.analysisStatus").value("PENDING"));
    }

    @Test
    void rejectsEmotionOutsideFixedNineValues() throws Exception {
        Member member = saveMember("invalid-emotion", "01000000005");
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(post("/api/diaries")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"오늘의 기록\",\"selectedEmotion\":\"질투나요\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DIARY_400"));
    }

    @Test
    void returnsOnlyAuthenticatedMembersDiariesNewestFirst() throws Exception {
        Member owner = saveMember("list-owner", "01000000006");
        Member another = saveMember("list-another", "01000000007");
        diaryService.create(owner.getPhoneNumber(), new CreateDiaryRequest("첫 번째 일기", "HAPPY"));
        diaryService.create(owner.getPhoneNumber(), new CreateDiaryRequest("두 번째 일기", "ANXIOUS"));
        diaryService.create(another.getPhoneNumber(), new CreateDiaryRequest("다른 사용자 일기", "SAD"));
        String token = jwtTokenProvider.createToken(owner.getPhoneNumber(), owner.getRole().name());

        mockMvc.perform(get("/api/diaries")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].content").value("두 번째 일기"))
                .andExpect(jsonPath("$.data[0].selectedEmotion").value("ANXIOUS"))
                .andExpect(jsonPath("$.data[0].selectedEmotionLabel").value("불안해요"))
                .andExpect(jsonPath("$.data[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$.data[1].content").value("첫 번째 일기"));
    }

    @Test
    void rejectsUnauthenticatedDiaryListQuery() throws Exception {
        mockMvc.perform(get("/api/diaries"))
                .andExpect(status().isForbidden());
    }

    @Test
    void hidesAnotherMembersAnalysis() throws Exception {
        Member owner = saveMember("owner2", "01000000002");
        Member stranger = saveMember("stranger", "01000000003");
        CreateDiaryResponse created = diaryService.create(owner.getPhoneNumber(), new CreateDiaryRequest("나만의 일기", "슬픔"));
        String strangerToken = jwtTokenProvider.createToken(stranger.getPhoneNumber(), stranger.getRole().name());

        mockMvc.perform(get("/api/diaries/{diaryId}/analysis", created.diaryId())
                        .header("Authorization", "Bearer " + strangerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DIARY_404"));
    }

    @Test
    void returnsNullAmuletFieldsUntilAnAmuletIsActuallyGenerated() throws Exception {
        Member member = saveMember("amulet", "01000000004");
        CreateDiaryResponse created = diaryService.create(
                member.getPhoneNumber(),
                new CreateDiaryRequest("친구와 즐거운 하루를 보냈다", "기쁨")
        );
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

                mockMvc.perform(get("/api/diaries/{diaryId}/analysis", created.diaryId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amuletId").value(nullValue()))
                .andExpect(jsonPath("$.data.amuletType").value(nullValue()))
                .andExpect(jsonPath("$.data.title").value(nullValue()))
                .andExpect(jsonPath("$.data.modelName").value(nullValue()))
                .andExpect(jsonPath("$.data.failureCode").value(nullValue()))
                .andExpect(jsonPath("$.data.attemptCount").value(0))
                .andExpect(jsonPath("$.data.createdAt").isString())
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty());
    }

    private Member saveMember(String name, String phoneNumber) {
        return memberRepository.save(Member.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .passwordHash("encoded-password")
                .role(Member.Role.ROLE_USER)
                .build());
    }
}
