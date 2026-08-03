package com.maumbujeok.backend.domain.diary.controller;

import com.maumbujeok.backend.domain.diary.dto.CreateDiaryResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryAnalysisResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryCalendarResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryCursorPageResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryDetailResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryResponse;
import com.maumbujeok.backend.domain.diary.dto.EmotionStatResponse;
import com.maumbujeok.backend.domain.diary.dto.UpdateDiaryResponse;
import com.maumbujeok.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

final class DiarySwaggerSchemas {
    private DiarySwaggerSchemas() {
    }

    @Schema(name = "CreateDiaryApiResponse", description = "\uC77C\uAE30 \uC0DD\uC131 \uACF5\uD1B5 \uC751\uB2F5")
    static final class CreateDiaryApiResponse extends ApiResponse<CreateDiaryResponse> {
        private CreateDiaryApiResponse() { super(true, "200", "\uC694\uCCAD\uC774 \uC131\uACF5\uD588\uC2B5\uB2C8\uB2E4.", null); }
    }

    @Schema(name = "DiaryCursorPageApiResponse", description = "\uC77C\uAE30 \uCEE4\uC11C \uBAA9\uB85D \uACF5\uD1B5 \uC751\uB2F5")
    static final class DiaryCursorPageApiResponse extends ApiResponse<DiaryCursorPageResponse> {
        private DiaryCursorPageApiResponse() { super(true, "200", "\uC694\uCCAD\uC774 \uC131\uACF5\uD588\uC2B5\uB2C8\uB2E4.", null); }
    }

    @Schema(name = "DiaryDateListApiResponse", description = "\uAE30\uB85D\uC77C\uBCC4 \uC77C\uAE30 \uBAA9\uB85D \uACF5\uD1B5 \uC751\uB2F5")
    static final class DiaryDateListApiResponse extends ApiResponse<List<DiaryResponse>> {
        private DiaryDateListApiResponse() { super(true, "200", "\uC694\uCCAD\uC774 \uC131\uACF5\uD588\uC2B5\uB2C8\uB2E4.", null); }
    }

    @Schema(name = "DiaryCalendarApiResponse", description = "\uC6D4\uBCC4 \uC77C\uAE30 \uCE98\uB9B0\uB354 \uACF5\uD1B5 \uC751\uB2F5")
    static final class DiaryCalendarApiResponse extends ApiResponse<DiaryCalendarResponse> {
        private DiaryCalendarApiResponse() { super(true, "200", "\uC694\uCCAD\uC774 \uC131\uACF5\uD588\uC2B5\uB2C8\uB2E4.", null); }
    }

    @Schema(name = "DiaryDetailApiResponse", description = "\uC77C\uAE30 \uC0C1\uC138 \uACF5\uD1B5 \uC751\uB2F5")
    static final class DiaryDetailApiResponse extends ApiResponse<DiaryDetailResponse> {
        private DiaryDetailApiResponse() { super(true, "200", "\uC694\uCCAD\uC774 \uC131\uACF5\uD588\uC2B5\uB2C8\uB2E4.", null); }
    }

    @Schema(name = "UpdateDiaryApiResponse", description = "\uC77C\uAE30 \uC218\uC815 \uACF5\uD1B5 \uC751\uB2F5")
    static final class UpdateDiaryApiResponse extends ApiResponse<UpdateDiaryResponse> {
        private UpdateDiaryApiResponse() { super(true, "200", "\uC694\uCCAD\uC774 \uC131\uACF5\uD588\uC2B5\uB2C8\uB2E4.", null); }
    }

    @Schema(name = "DiaryEmotionStatsApiResponse", description = "\uAC10\uC815 \uD1B5\uACC4 \uACF5\uD1B5 \uC751\uB2F5")
    static final class DiaryEmotionStatsApiResponse extends ApiResponse<List<EmotionStatResponse>> {
        private DiaryEmotionStatsApiResponse() { super(true, "200", "\uC694\uCCAD\uC774 \uC131\uACF5\uD588\uC2B5\uB2C8\uB2E4.", null); }
    }

    @Schema(name = "DiaryAnalysisApiResponse", description = "\uC77C\uAE30 AI \uBD84\uC11D \uACF5\uD1B5 \uC751\uB2F5")
    static final class DiaryAnalysisApiResponse extends ApiResponse<DiaryAnalysisResponse> {
        private DiaryAnalysisApiResponse() { super(true, "200", "\uC694\uCCAD\uC774 \uC131\uACF5\uD588\uC2B5\uB2C8\uB2E4.", null); }
    }

    @Schema(name = "DiaryErrorApiResponse", description = "\uC77C\uAE30 API \uC624\uB958 \uACF5\uD1B5 \uC751\uB2F5")
    static final class DiaryErrorApiResponse extends ApiResponse<Void> {
        private DiaryErrorApiResponse() { super(false, "DIARY_400", "\uC77C\uAE30 \uC694\uCCAD\uC774 \uC798\uBABB\uB418\uC5C8\uC2B5\uB2C8\uB2E4.", null); }
    }
}