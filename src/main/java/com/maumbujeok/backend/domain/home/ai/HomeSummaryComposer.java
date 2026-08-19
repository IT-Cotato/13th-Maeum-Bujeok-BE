package com.maumbujeok.backend.domain.home.ai;

import com.maumbujeok.backend.domain.home.domain.PrimaryElement;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class HomeSummaryComposer {

    public HomeSummaryAiResponse compose(String memberName, String gender, String calendarType,
                                         String birthDate, String birthTime, LocalDate summaryDate,
                                         List<TodayDiaryInput> todayDiaries) {
        int day = summaryDate != null ? summaryDate.getDayOfMonth() : 1;
        PrimaryElement element = PrimaryElement.values()[Math.abs(day) % PrimaryElement.values().length];

        String name = (memberName != null && !memberName.isBlank()) ? memberName : "사용자";

        if (todayDiaries != null && !todayDiaries.isEmpty()) {
            TodayDiaryInput latestDiary = todayDiaries.get(todayDiaries.size() - 1);
            String emotionHint = latestDiary.emotion() != null ? latestDiary.emotion() : "기록된 감정";
            return new HomeSummaryAiResponse(
                    element,
                    "오늘 남겨주신 소중한 마음 기록을 바탕으로 차분히 내면을 정리해 보세요.",
                    String.format("오늘 %s님께서 남겨주신 %s의 감정은 %s 기운과 조화를 이루며 마음의 균형을 찾아가고 있습니다. 스스로의 감정을 솔직하게 마주한 오늘 하루에 따뜻한 격려를 보내주세요.",
                            name, emotionHint, element.getTitle())
            );
        }

        return switch (element) {
            case WOOD -> new HomeSummaryAiResponse(
                    element,
                    "오늘 하루는 창가 가까이에서 햇살을 받으며 따뜻한 차 한 잔을 기울여 보세요.",
                    name + "님 사주에 뻗어나가는 목(木)의 성장의 기운이 함께하여 새로운 도전에 시너지를 줍니다. 급한 마음보다는 씨앗을 심듯 조급함을 내려놓고 차근차근 계획을 실행에 옮기시기 바랍니다."
            );
            case FIRE -> new HomeSummaryAiResponse(
                    element,
                    "가까운 동료나 친구에게 밝은 표정으로 가벼운 안부를 전해 보세요.",
                    "오늘 " + name + "님의 기운에는 열정적인 화(火)의 기운이 솟구쳐 자신감과 표현력이 한층 새로워집니다. 충동적인 판단을 피하기 위해 중요한 결정 앞에서는 깊은 숨을 내쉬며 한 걸음 물러서는 지혜가 도움이 됩니다."
            );
            case EARTH -> new HomeSummaryAiResponse(
                    element,
                    "책상 주변이나 자주 머무는 공간을 깔끔하게 정리하며 마음의 안정을 찾으세요.",
                    "포용력과 단단함을 상징하는 토(土)의 기운이 " + name + "님의 마음을 든든하게 지지해 줍니다. 묵묵히 내 자리를 지키며 차분하게 일과를 정리하면 뜻밖의 결실과 평온을 누릴 수 있습니다."
            );
            case METAL -> new HomeSummaryAiResponse(
                    element,
                    "우선순위가 높은 일 하나에 집중하여 깔끔하게 마무리해 보세요.",
                    "선명하고 절제된 금(金)의 기운이 작용하여 " + name + "님의 명확한 이성과 결단력을 높여줍니다. 불필요한 고민에 마음을 빼앗기지 말고 스스로 판단한 기준을 신뢰하며 나아가세요."
            );
            case WATER -> new HomeSummaryAiResponse(
                    element,
                    "물 한 잔을 천천히 마시며 어깨의 긴장을 풀고 편안한 음악을 들어보세요.",
                    "깊고 유연한 수(水)의 기운이 흐르는 오늘, " + name + "님의 통찰력과 직관이 풍부해집니다. 주변의 유혹이나 거센 기운에 휩쓸리지 않고 조용히 자신의 내면을 들여다보는 휴식이 큰 힘이 됩니다."
            );
        };
    }
}
