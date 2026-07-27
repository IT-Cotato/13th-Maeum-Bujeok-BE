package com.maumbujeok.backend.domain.upload.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.global.security.JwtTokenProvider;
import java.net.URI;
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
class UploadControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;

    @Test
    void issuesAttachesAndDeletesOwnedImage() throws Exception {
        Member owner = saveMember("upload-owner", "01000000031");
        Member stranger = saveMember("upload-stranger", "01000000032");
        String ownerToken = token(owner);

        String response = mockMvc.perform(post("/api/uploads/presigned-url")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentType\":\"image/png\",\"fileSize\":1024}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uploadUrl").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String uploadId = JsonPath.read(response, "$.data.uploadId");
        String uploadUrl = JsonPath.read(response, "$.data.uploadUrl");

        mockMvc.perform(put(URI.create(uploadUrl).getPath())
                        .contentType(MediaType.IMAGE_PNG)
                        .content(new byte[1024]))
                .andExpect(status().isNoContent());

        String diaryResponse = mockMvc.perform(post("/api/diaries")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"with image\",\"selectedEmotion\":\"HAPPY\",\"recordedDate\":\"2026-07-10\",\"imageUploadIds\":[\"" + uploadId + "\"]}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Integer diaryId = JsonPath.read(diaryResponse, "$.data.diaryId");

        mockMvc.perform(get("/api/diaries/{diaryId}", diaryId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.images", hasSize(1)))
                .andExpect(jsonPath("$.data.analysis.status").value("PENDING"));

        mockMvc.perform(delete("/api/uploads/{uploadId}", uploadId)
                        .header("Authorization", "Bearer " + token(stranger)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/diaries/{diaryId}", diaryId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(jsonPath("$.data.images", hasSize(1)));

        mockMvc.perform(delete("/api/uploads/{uploadId}", uploadId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/diaries/{diaryId}", diaryId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(jsonPath("$.data.images", hasSize(0)));
    }

    @Test
    void rejectsUnsupportedImageTypeAndOversizedFile() throws Exception {
        Member member = saveMember("invalid-upload", "01000000033");
        mockMvc.perform(post("/api/uploads/presigned-url")
                        .header("Authorization", "Bearer " + token(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentType\":\"image/gif\",\"fileSize\":1024}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UPLOAD_400"));
    }

    private String token(Member member) {
        return jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());
    }

    private Member saveMember(String name, String phoneNumber) {
        return memberRepository.save(Member.builder()
                .name(name).phoneNumber(phoneNumber).passwordHash("encoded-password")
                .role(Member.Role.ROLE_USER).build());
    }
}
