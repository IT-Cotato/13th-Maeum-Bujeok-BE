package com.maumbujeok.backend.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public record NextWeekFlowRequest(
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate weekStart
) {
}
