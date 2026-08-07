package com.maumbujeok.backend.domain.saju.ai;

import java.util.Arrays;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "fake", matchIfMissing = true)
public class FakeSajuAiProvider implements SajuAiProvider {
    @Override
    public SajuAiCallResult analyze(SajuAiRequest request) {
        int seed = Math.abs(
                (request.birthDate() + "|" + request.gender() + "|" + request.calendarType() + "|" + request.birthTime())
                        .hashCode()
        );

        int[] raw = new int[] {
                10 + Math.floorMod(seed, 23),
                10 + Math.floorMod(seed / 3, 23),
                10 + Math.floorMod(seed / 5, 23),
                10 + Math.floorMod(seed / 7, 23),
                10 + Math.floorMod(seed / 11, 23)
        };

        int[] percentages = normalize(raw);
        return new SajuAiCallResult(
                new SajuAiResult(
                        percentages[0],
                        percentages[1],
                        percentages[2],
                        percentages[3],
                        percentages[4],
                        "fake-saju-v1"
                ),
                1
        );
    }

    private int[] normalize(int[] raw) {
        int sum = Arrays.stream(raw).sum();
        int[] normalized = new int[raw.length];
        int assigned = 0;
        for (int index = 0; index < raw.length; index++) {
            normalized[index] = raw[index] * 100 / sum;
            assigned += normalized[index];
        }

        int remainder = 100 - assigned;
        for (int index = 0; index < remainder; index++) {
            normalized[index % normalized.length] += 1;
        }
        return normalized;
    }
}
