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
    private DiarySwaggerSchemas() {}

    @Schema(name = "CreateDiaryApiResponse", description = "일기 생성 공통 응답")
    static final class CreateDiaryApiResponse extends ApiResponse<CreateDiaryResponse> { private CreateDiaryApiResponse() { super(true, "200", "요청이 성공하였습니다.", null); } }
    @Schema(name = "DiaryCursorPageApiResponse", description = "일기 커서 목록 공통 응답")
    static final class DiaryCursorPageApiResponse extends ApiResponse<DiaryCursorPageResponse> { private DiaryCursorPageApiResponse() { super(true, "200", "요청이 성공하였습니다.", null); } }
    @Schema(name = "DiaryDateListApiResponse", description = "기록일별 일기 목록 공통 응답")
    static final class DiaryDateListApiResponse extends ApiResponse<List<DiaryResponse>> { private DiaryDateListApiResponse() { super(true, "200", "요청이 성공하였습니다.", null); } }
    @Schema(name = "DiaryCalendarApiResponse", description = "월별 일기 달력 공통 응답")
    static final class DiaryCalendarApiResponse extends ApiResponse<DiaryCalendarResponse> { private DiaryCalendarApiResponse() { super(true, "200", "요청이 성공하였습니다.", null); } }
    @Schema(name = "DiaryDetailApiResponse", description = "일기 상세 공통 응답")
    static final class DiaryDetailApiResponse extends ApiResponse<DiaryDetailResponse> { private DiaryDetailApiResponse() { super(true, "200", "요청이 성공하였습니다.", null); } }
    @Schema(name = "UpdateDiaryApiResponse", description = "일기 수정 공통 응답")
    static final class UpdateDiaryApiResponse extends ApiResponse<UpdateDiaryResponse> { private UpdateDiaryApiResponse() { super(true, "200", "요청이 성공하였습니다.", null); } }
    @Schema(name = "DiaryEmotionStatsApiResponse", description = "감정 통계 공통 응답")
    static final class DiaryEmotionStatsApiResponse extends ApiResponse<List<EmotionStatResponse>> { private DiaryEmotionStatsApiResponse() { super(true, "200", "요청이 성공하였습니다.", null); } }
    @Schema(name = "DiaryAnalysisApiResponse", description = "일기 AI 분석 공통 응답")
    static final class DiaryAnalysisApiResponse extends ApiResponse<DiaryAnalysisResponse> { private DiaryAnalysisApiResponse() { super(true, "200", "요청이 성공하였습니다.", null); } }

    @Schema(name = "DiaryInvalidRequestApiResponse", description = "DIARY_400 오류 응답")
    static final class DiaryInvalidRequestApiResponse extends ApiResponse<Void> { private DiaryInvalidRequestApiResponse() { super(false, "DIARY_400", "일기 요청이 올바르지 않습니다.", null); } }
    @Schema(name = "DiaryCursorErrorApiResponse", description = "DIARY_CURSOR_400 오류 응답")
    static final class DiaryCursorErrorApiResponse extends ApiResponse<Void> { private DiaryCursorErrorApiResponse() { super(false, "DIARY_CURSOR_400", "일기 목록 커서가 올바르지 않습니다.", null); } }
    @Schema(name = "DiaryNotFoundApiResponse", description = "DIARY_404 오류 응답")
    static final class DiaryNotFoundApiResponse extends ApiResponse<Void> { private DiaryNotFoundApiResponse() { super(false, "DIARY_404", "일기를 찾을 수 없습니다.", null); } }
    @Schema(name = "UploadInvalidRequestApiResponse", description = "UPLOAD_400 오류 응답")
    static final class UploadInvalidRequestApiResponse extends ApiResponse<Void> { private UploadInvalidRequestApiResponse() { super(false, "UPLOAD_400", "이미지 업로드 요청이 올바르지 않습니다.", null); } }
    @Schema(name = "UploadNotFoundApiResponse", description = "UPLOAD_404 오류 응답")
    static final class UploadNotFoundApiResponse extends ApiResponse<Void> { private UploadNotFoundApiResponse() { super(false, "UPLOAD_404", "업로드 정보를 찾을 수 없습니다.", null); } }
    @Schema(name = "UploadStorageErrorApiResponse", description = "UPLOAD_500 오류 응답")
    static final class UploadStorageErrorApiResponse extends ApiResponse<Void> { private UploadStorageErrorApiResponse() { super(false, "UPLOAD_500", "이미지 저장소 처리 중 오류가 발생했습니다.", null); } }
}