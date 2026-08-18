package com.maumbujeok.backend.domain.member.service;

import com.maumbujeok.backend.domain.member.dto.TermsDetailResponse;
import com.maumbujeok.backend.domain.member.dto.TermsSummaryResponse;
import com.maumbujeok.backend.global.error.CustomException;
import com.maumbujeok.backend.global.error.ErrorCode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class TermsService {

    public List<TermsSummaryResponse> getTerms() {
        return Arrays.stream(TermType.values())
                .map(TermType::toSummary)
                .toList();
    }

    public TermsDetailResponse getTermsDetail(String termType) {
        return TermType.from(termType).toDetail();
    }

    private enum TermType {
        SERVICE_TERMS("서비스 이용약관", true, "v1.0", null, "terms/service-terms-v1.md");

        private final String title;
        private final boolean required;
        private final String version;
        private final LocalDate effectiveDate;
        private final String resourcePath;

        TermType(String title, boolean required, String version, LocalDate effectiveDate, String resourcePath) {
            this.title = title;
            this.required = required;
            this.version = version;
            this.effectiveDate = effectiveDate;
            this.resourcePath = resourcePath;
        }

        static TermType from(String value) {
            try {
                return TermType.valueOf(value);
            } catch (IllegalArgumentException | NullPointerException exception) {
                throw new CustomException(ErrorCode.TERMS_NOT_FOUND);
            }
        }

        TermsSummaryResponse toSummary() {
            return new TermsSummaryResponse(name(), title, required, version, effectiveDate);
        }

        TermsDetailResponse toDetail() {
            return new TermsDetailResponse(name(), title, required, version, effectiveDate, readContent());
        }

        private String readContent() {
            try {
                return new ClassPathResource(resourcePath).getContentAsString(StandardCharsets.UTF_8);
            } catch (IOException exception) {
                throw new IllegalStateException("Terms content could not be loaded", exception);
            }
        }
    }
}
