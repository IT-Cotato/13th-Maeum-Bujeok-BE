package com.maumbujeok.backend.domain.home.ai;

import com.maumbujeok.backend.domain.home.domain.PrimaryElement;

public record HomeSummaryAiResponse(
        PrimaryElement primaryElement,
        String todayLuck,
        String todayEnergy
) {
}
