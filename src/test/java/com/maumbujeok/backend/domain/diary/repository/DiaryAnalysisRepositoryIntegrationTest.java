package com.maumbujeok.backend.domain.diary.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysis;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysisStatus;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("integration")
@Testcontainers
@Transactional
class DiaryAnalysisRepositoryIntegrationTest {
    @Container
    @ServiceConnection
    static final MySQLContainer MYSQL = new MySQLContainer(DockerImageName.parse("mysql:8.4"));

    @Autowired MemberRepository memberRepository;
    @Autowired DiaryRepository diaryRepository;
    @Autowired DiaryAnalysisRepository analysisRepository;

    @Test
    void storesOnePendingAnalysisPerDiaryOnMySql() {
        Member member = memberRepository.save(Member.builder()
                .loginId("diary")
                .phoneNumber("01000000005")
                .passwordHash("encoded")
                .role(Member.Role.ROLE_USER)
                .build());
        Diary diary = diaryRepository.save(new Diary(member, "오늘의 일기", "불안"));
        DiaryAnalysis first = analysisRepository.saveAndFlush(new DiaryAnalysis(diary, "diary-v1", "policy-v1"));

        assertEquals(DiaryAnalysisStatus.PENDING, first.getStatus());
        assertThrows(DataIntegrityViolationException.class, () ->
                analysisRepository.saveAndFlush(new DiaryAnalysis(diary, "diary-v1", "policy-v1"))
        );
    }
}
