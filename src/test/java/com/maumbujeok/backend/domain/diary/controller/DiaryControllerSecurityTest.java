package com.maumbujeok.backend.domain.diary.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maumbujeok.backend.domain.diary.ai.DiaryAiResult;
import com.maumbujeok.backend.domain.diary.application.DiaryService;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysis;
import com.maumbujeok.backend.domain.diary.domain.SafetyLevel;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryRequest;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryResponse;
import com.maumbujeok.backend.domain.diary.repository.DiaryAnalysisRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.global.ai.emotion.ReportEmotion;
import com.maumbujeok.backend.global.security.JwtTokenProvider;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
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
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired DiaryService diaryService;
    @Autowired DiaryAnalysisRepository analysisRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;

    @Test
    void rejectsUnauthenticatedDiaryCreation() throws Exception {
        mockMvc.perform(post("/api/diaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"오늘의 기록\",\"selectedEmotion\":\"불안\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedMemberCreatesPastDiaryWithPendingAnalysis() throws Exception {
        Member member = saveMember("owner", "01000000001");
        String token = token(member);

        mockMvc.perform(post("/api/diaries")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "오늘은 불안했지만 잘 견뎠다",
                                  "selectedEmotion": "ANXIOUS",
                                  "recordedDate": "2026-07-20"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recordedDate").value("2026-07-20"))
                .andExpect(jsonPath("$.data.analysisStatus").value("PENDING"));
    }

    @Test
    void defaultsRecordedDateToToday() throws Exception {
        Member member = saveMember("today", "01000000011");

        mockMvc.perform(post("/api/diaries")
                        .header("Authorization", "Bearer " + token(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"오늘의 기록\",\"selectedEmotion\":\"HAPPY\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recordedDate")
                        .value(LocalDate.now(SERVICE_ZONE).toString()));
    }

    @Test
    void rejectsFutureRecordedDate() throws Exception {
        Member member = saveMember("future", "01000000012");
        String future = LocalDate.now(SERVICE_ZONE).plusDays(1).toString();

        mockMvc.perform(post("/api/diaries")
                        .header("Authorization", "Bearer " + token(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"미래의 기록","selectedEmotion":"HAPPY","recordedDate":"%s"}
                                """.formatted(future)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DIARY_400"));
    }

    @Test
    void rejectsEmotionOutsideFixedNineValues() throws Exception {
        Member member = saveMember("invalid-emotion", "01000000005");

        mockMvc.perform(post("/api/diaries")
                        .header("Authorization", "Bearer " + token(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"오늘의 기록\",\"selectedEmotion\":\"질투나요\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DIARY_400"));
    }

    @Test
    void returnsAllDateFilteredDiariesFromDedicatedEndpoint() throws Exception {
        Member owner = saveMember("list-owner", "01000000006");
        Member another = saveMember("list-another", "01000000007");
        diaryService.create(owner.getPhoneNumber(), request("first", "HAPPY", "2026-07-20"));
        diaryService.create(owner.getPhoneNumber(), request("second", "ANXIOUS", "2026-07-20"));
        diaryService.create(owner.getPhoneNumber(), request("other day", "SAD", "2026-07-19"));
        diaryService.create(another.getPhoneNumber(), request("other member", "SAD", "2026-07-20"));

        mockMvc.perform(get("/api/diaries/by-date")
                        .param("date", "2026-07-20")
                        .header("Authorization", "Bearer " + token(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].content").value("second"))
                .andExpect(jsonPath("$.data[0].recordedDate").value("2026-07-20"))
                .andExpect(jsonPath("$.data[1].content").value("first"))
                .andExpect(jsonPath("$.data[1].recordedDate").value("2026-07-20"));
    }
    @Test
    void getsOnlyOwnedDiaryDetail() throws Exception {
        Member owner = saveMember("detail-owner", "01000000013");
        Member stranger = saveMember("detail-stranger", "01000000014");
        CreateDiaryResponse created = diaryService.create(
                owner.getPhoneNumber(),
                request("나의 일기", "COMFORTABLE", "2026-07-18")
        );

        mockMvc.perform(get("/api/diaries/{diaryId}", created.diaryId())
                        .header("Authorization", "Bearer " + token(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("나의 일기"))
                .andExpect(jsonPath("$.data.recordedDate").value("2026-07-18"));

        mockMvc.perform(get("/api/diaries/{diaryId}", created.diaryId())
                        .header("Authorization", "Bearer " + token(stranger)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DIARY_404"));
    }

    @Test
    void updatesContentAndEmotionWithoutChangingRecordedDate() throws Exception {
        Member member = saveMember("update", "01000000015");
        CreateDiaryResponse created = diaryService.create(
                member.getPhoneNumber(),
                request("수정 전", "ANXIOUS", "2026-07-17")
        );

        mockMvc.perform(patch("/api/diaries/{diaryId}", created.diaryId())
                        .header("Authorization", "Bearer " + token(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"수정 후\",\"selectedEmotion\":\"COMFORTABLE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recordedDate").value("2026-07-17"))
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.analysisStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.analysisRestarted").value(true));

        mockMvc.perform(get("/api/diaries/{diaryId}", created.diaryId())
                        .header("Authorization", "Bearer " + token(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("수정 후"))
                .andExpect(jsonPath("$.data.selectedEmotion").value("COMFORTABLE"))
                .andExpect(jsonPath("$.data.recordedDate").value("2026-07-17"));
    }

    @Test
    void doesNotRestartAnalysisWhenPatchDoesNotChangeValues() throws Exception {
        Member member = saveMember("same-update", "01000000016");
        CreateDiaryResponse created = diaryService.create(
                member.getPhoneNumber(),
                request("같은 내용", "HAPPY", "2026-07-16")
        );

        mockMvc.perform(patch("/api/diaries/{diaryId}", created.diaryId())
                        .header("Authorization", "Bearer " + token(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"같은 내용\",\"selectedEmotion\":\"HAPPY\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.analysisRestarted").value(false));
    }

    @Test
    void returnsNineReportEmotionStatsAndExcludesPendingAnalysis() throws Exception {
        Member member = saveMember("stats", "01000000017");
        CreateDiaryResponse completedDiary = diaryService.create(
                member.getPhoneNumber(),
                request("불안한 기록", "ANXIOUS", "2026-07-15")
        );
        diaryService.create(
                member.getPhoneNumber(),
                request("분석 대기 기록", "HAPPY", "2026-07-16")
        );

        DiaryAnalysis analysis = analysisRepository
                .findByDiaryIdAndDiaryMemberPhoneNumber(
                        completedDiary.diaryId(),
                        member.getPhoneNumber()
                )
                .orElseThrow();
        analysis.markProcessing(analysis.getInputRevision());
        analysis.complete(
                analysis.getInputRevision(),
                result(ReportEmotion.ANXIETY),
                50,
                false,
                1,
                SafetyLevel.NORMAL
        );
        analysisRepository.flush();

        mockMvc.perform(get("/api/diaries/emotion-stats")
                        .param("from", "2026-07-15")
                        .param("to", "2026-07-16")
                        .header("Authorization", "Bearer " + token(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(9)))
                .andExpect(jsonPath("$.data[5].emotion").value("불안"))
                .andExpect(jsonPath("$.data[5].count").value(1));
    }

    @Test
    void hidesAnotherMembersAnalysis() throws Exception {
        Member owner = saveMember("owner2", "01000000002");
        Member stranger = saveMember("stranger", "01000000003");
        CreateDiaryResponse created = diaryService.create(
                owner.getPhoneNumber(),
                new CreateDiaryRequest("나만의 일기", "슬픔")
        );

        mockMvc.perform(get("/api/diaries/{diaryId}/analysis", created.diaryId())
                        .header("Authorization", "Bearer " + token(stranger)))
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

        mockMvc.perform(get("/api/diaries/{diaryId}/analysis", created.diaryId())
                        .header("Authorization", "Bearer " + token(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amuletId").value(nullValue()))
                .andExpect(jsonPath("$.data.amuletType").value(nullValue()))
                .andExpect(jsonPath("$.data.title").value(nullValue()))
                .andExpect(jsonPath("$.data.modelName").value(nullValue()))
                .andExpect(jsonPath("$.data.failureCode").value(nullValue()))
                .andExpect(jsonPath("$.data.attemptCount").value(0));
    }

    @Test
    void allowsSecondDiaryForSameRecordedDate() throws Exception {
        Member member = saveMember("duplicate-date", "01000000018");
        diaryService.create(member.getPhoneNumber(), request("first", "HAPPY", "2026-07-14"));

        mockMvc.perform(post("/api/diaries")
                        .header("Authorization", "Bearer " + token(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"second\",\"selectedEmotion\":\"SAD\",\"recordedDate\":\"2026-07-14\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recordedDate").value("2026-07-14"))
                .andExpect(jsonPath("$.data.analysisStatus").value("PENDING"));
    }

    @Test
    void returnsCalendarAndCursorPage() throws Exception {
        Member member = saveMember("calendar", "01000000019");
        diaryService.create(member.getPhoneNumber(), request("newer", "HAPPY", "2026-07-13"));
        diaryService.create(member.getPhoneNumber(), request("older", "SAD", "2026-07-12"));

        mockMvc.perform(get("/api/diaries/calendar")
                        .param("year", "2026").param("month", "7")
                        .header("Authorization", "Bearer " + token(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.days", hasSize(2)))
                .andExpect(jsonPath("$.data.days[0].status").value("STORED"));

        mockMvc.perform(get("/api/diaries")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + token(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].content").value("newer"))
                .andExpect(jsonPath("$.data.nextCursor").isNotEmpty())
                .andExpect(jsonPath("$.data.hasNext").value(true));
    }

    @Test
    void deletesOwnedDiary() throws Exception {
        Member member = saveMember("delete", "01000000020");
        CreateDiaryResponse created = diaryService.create(
                member.getPhoneNumber(), request("delete me", "NORMAL", "2026-07-11"));

        mockMvc.perform(delete("/api/diaries/{diaryId}", created.diaryId())
                        .header("Authorization", "Bearer " + token(member)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/diaries/{diaryId}", created.diaryId())
                        .header("Authorization", "Bearer " + token(member)))
                .andExpect(status().isNotFound());
    }
    private CreateDiaryRequest request(String content, String emotion, String date) {
        return new CreateDiaryRequest(content, emotion, LocalDate.parse(date));
    }

    private DiaryAiResult result(ReportEmotion emotion) {
        return new DiaryAiResult(
                "오늘의 마음을 견디느라 애썼어요.",
                "오늘의 마음을 천천히 되돌아본 하루의 기록",
                50,
                List.of(),
                emotion,
                SafetyLevel.NORMAL,
                "test"
        );
    }

    private String token(Member member) {
        return jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());
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
