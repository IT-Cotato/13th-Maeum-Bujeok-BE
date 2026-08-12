package com.maumbujeok.backend.domain.home.controller;

import com.maumbujeok.backend.domain.home.application.HomeSummaryService;
import com.maumbujeok.backend.domain.home.domain.PrimaryElement;
import com.maumbujeok.backend.domain.home.dto.HomeSummaryResponse;
import com.maumbujeok.backend.global.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

class HomeControllerTest {

    @Test
    @DisplayName("홈 화면 요약 조회 컨트롤러 단위 테스트")
    void getTodayHomeSummary_success() {
        // given
        HomeSummaryService service = Mockito.mock(HomeSummaryService.class);
        com.maumbujeok.backend.domain.diary.repository.DiaryRepository diaryRepository = Mockito.mock(com.maumbujeok.backend.domain.diary.repository.DiaryRepository.class);
        com.maumbujeok.backend.domain.talisman.repository.TalismanRepository talismanRepository = Mockito.mock(com.maumbujeok.backend.domain.talisman.repository.TalismanRepository.class);
        HomeController controller = new HomeController(service, diaryRepository, talismanRepository);
        CustomUserDetails userDetails = Mockito.mock(CustomUserDetails.class);
        com.maumbujeok.backend.domain.member.domain.Member member =
                Mockito.mock(com.maumbujeok.backend.domain.member.domain.Member.class);

        given(userDetails.getMember()).willReturn(member);
        given(member.getPhoneNumber()).willReturn("01012345678");

        HomeSummaryResponse expectedResponse = new HomeSummaryResponse(
                PrimaryElement.FIRE,
                "오늘의 행운 팁 1문장입니다.",
                "오행 기반 감정 분석 첫 번째 문장입니다. 두 번째 문장입니다."
        );
        given(service.getTodaySummary(eq("01012345678"))).willReturn(expectedResponse);

        // when
        var response = controller.getTodayHomeSummary(userDetails);

        // then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(PrimaryElement.FIRE, response.getData().primaryElement());
        assertEquals("오늘의 행운 팁 1문장입니다.", response.getData().todayLuck());
        assertEquals("오행 기반 감정 분석 첫 번째 문장입니다. 두 번째 문장입니다.", response.getData().todayEnergy());
    }
}
