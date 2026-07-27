package com.maumbujeok.backend.domain.report.ai;

import com.maumbujeok.backend.domain.report.application.WeeklyEmotionSnapshot;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class WeeklyReportSummaryComposer {

    public WeeklyReportAiResult compose(WeeklyReportAiRequest request, String modelName) {
        WeeklyEmotionSnapshot snapshot = request.emotionSnapshot();
        SentimentTone tone = resolveTone(snapshot);

        String title = switch (tone) {
            case POSITIVE -> String.format("이번 주는 %s에게 꽤 괜찮은 한 주였어요!", request.memberDisplayName());
            case NEUTRAL -> String.format("이번 주는 %s에게 차분히 균형을 잡아간 한 주였어요.", request.memberDisplayName());
            case NEGATIVE -> String.format("이번 주는 %s에게 마음을 다독일 시간이 필요했던 한 주였어요.", request.memberDisplayName());
        };

        String introduction = switch (tone) {
            case POSITIVE -> String.format("이번 주 %s의 기록에는 따뜻한 기운이 가득했어요.", request.memberDisplayName());
            case NEUTRAL -> String.format("이번 주 %s의 기록에는 잔잔하지만 단단한 흐름이 이어졌어요.", request.memberDisplayName());
            case NEGATIVE -> String.format("이번 주 %s의 기록에는 무거운 마음을 버텨낸 흔적이 남아 있었어요.", request.memberDisplayName());
        };

        String body = switch (tone) {
            case POSITIVE -> String.format(
                    Locale.US,
                    "%.1f%%가 긍정적인 감정으로 채워진 한 주였네요. 총 %d편의 기록에서 %s 마음이 자주 보였고, 일상 안에서 스스로를 다독이려는 힘도 함께 느껴졌어요. 이번 주의 좋은 흐름을 다음 주에도 이어가 보세요.",
                    snapshot.positiveRatio(),
                    snapshot.totalDiaryCount(),
                    snapshot.dominantEmotionLabel()
            );
            case NEUTRAL -> String.format(
                    Locale.US,
                    "%.1f%%가 비교적 잔잔한 감정으로 흐른 한 주였네요. 총 %d편의 기록에서 %s 마음이 중심을 이루며, 큰 흔들림보다는 천천히 자신을 살피는 시간이 이어졌어요. 지금의 균형감을 너무 조급해하지 말고 이어가 보세요.",
                    snapshot.neutralRatio(),
                    snapshot.totalDiaryCount(),
                    snapshot.dominantEmotionLabel()
            );
            case NEGATIVE -> String.format(
                    Locale.US,
                    "%.1f%%가 다소 무거운 감정으로 모인 한 주였네요. 총 %d편의 기록에서 %s 마음이 자주 드러났지만, 그만큼 자신의 상태를 솔직하게 바라본 힘도 분명히 있었어요. 다음 주에는 작은 휴식과 주변의 도움을 더 자주 챙겨보세요.",
                    snapshot.negativeRatio(),
                    snapshot.totalDiaryCount(),
                    snapshot.dominantEmotionLabel()
            );
        };

        return new WeeklyReportAiResult(title + "\n\n" + introduction + "\n\n" + body, modelName);
    }

    private SentimentTone resolveTone(WeeklyEmotionSnapshot snapshot) {
        if (snapshot.negativeRatio() > snapshot.positiveRatio() && snapshot.negativeRatio() >= snapshot.neutralRatio()) {
            return SentimentTone.NEGATIVE;
        }
        if (snapshot.positiveRatio() >= snapshot.neutralRatio()) {
            return SentimentTone.POSITIVE;
        }
        return SentimentTone.NEUTRAL;
    }

    private enum SentimentTone {
        POSITIVE, NEUTRAL, NEGATIVE
    }
}
