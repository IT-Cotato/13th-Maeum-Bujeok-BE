package com.maumbujeok.backend.domain.member.repository;

import com.maumbujeok.backend.domain.member.domain.Member;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("integration")
@Testcontainers
@Transactional
class MemberRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static final MySQLContainer MYSQL = new MySQLContainer(DockerImageName.parse("mysql:8.4"));

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void savesAndFindsMemberByPhoneNumberUsingMySql() {
        Member member = Member.builder()
                .name("harness")
                .phoneNumber("01000000006")
                .passwordHash("encoded-password")
                .role(Member.Role.ROLE_USER)
                .build();

        memberRepository.saveAndFlush(member);

        Optional<Member> found = memberRepository.findByPhoneNumber("01000000006");
        assertTrue(found.isPresent());
        assertEquals(Member.Role.ROLE_USER, found.orElseThrow().getRole());
    }
}
