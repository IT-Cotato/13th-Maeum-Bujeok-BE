package com.maumbujeok.backend.domain.report.dto;

import com.maumbujeok.backend.domain.talisman.dto.TalismanItemResponse;
import java.time.LocalDate;
import java.util.List;

public record ReportTalismansResponse(LocalDate periodStart, LocalDate periodEnd, List<TalismanItemResponse> items) {}