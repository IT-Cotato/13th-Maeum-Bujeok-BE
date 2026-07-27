package com.maumbujeok.backend.domain.diary.controller;

final class DiarySwaggerExamples {
    private DiarySwaggerExamples() {
    }

    static final String CREATE_SUCCESS = """
            {
              "success": true,
              "code": "200",
              "message": "요청에 성공하였습니다.",
              "data": {
                "diaryId": 42,
                "analysisStatus": "PENDING"
              }
            }
            """;

    static final String LIST_SUCCESS = """
            {
              "success": true,
              "code": "200",
              "message": "요청에 성공하였습니다.",
              "data": [
                {
                  "diaryId": 42,
                  "content": "오늘은 걱정이 많았지만 산책을 하며 마음을 가라앉혔다.",
                  "selectedEmotion": "ANXIOUS",
                  "selectedEmotionLabel": "불안해요",
                  "createdAt": "2026-07-23T21:15:30"
                }
              ]
            }
            """;

    static final String ANALYSIS_COMPLETED = """
            {
              "success": true,
              "code": "200",
              "message": "요청에 성공하였습니다.",
              "data": {
                "status": "COMPLETED",
                "summary": "걱정 속에서도 산책으로 마음을 돌본 하루",
                "empathyResponse": "복잡한 마음을 잘 다독이며 하루를 지나오셨네요.",
                "negativeIntensity": 35,
                "reportEmotion": "불안",
                "salpuriRecommended": false,
                "safetyLevel": "NORMAL",
                "amuletId": null,
                "amuletType": null,
                "title": null,
                "createdAt": "2026.07.23",
                "modelName": "gpt-5.5",
                "failureCode": null,
                "attemptCount": 1
              }
            }
            """;

    static final String ANALYSIS_PENDING = """
            {
              "success": true,
              "code": "200",
              "message": "요청에 성공하였습니다.",
              "data": {
                "status": "PENDING",
                "summary": null,
                "empathyResponse": null,
                "negativeIntensity": null,
                "reportEmotion": null,
                "salpuriRecommended": null,
                "safetyLevel": null,
                "amuletId": null,
                "amuletType": null,
                "title": null,
                "createdAt": "2026.07.23",
                "modelName": null,
                "failureCode": null,
                "attemptCount": 0
              }
            }
            """;

    static final String INVALID_CONTENT = """
            {
              "success": false,
              "code": "DIARY_400",
              "message": "일기 내용은 1자 이상 5000자 이하여야 합니다.",
              "data": null
            }
            """;

    static final String INVALID_EMOTION = """
            {
              "success": false,
              "code": "DIARY_400",
              "message": "선택 감정은 다음 9개 코드 중 하나여야 합니다: JOYFUL, HAPPY, EXCITED, COMFORTABLE, NORMAL, LETHARGIC, SAD, ANXIOUS, ANGRY",
              "data": null
            }
            """;

    static final String DIARY_NOT_FOUND = """
            {
              "success": false,
              "code": "DIARY_404",
              "message": "일기를 찾을 수 없습니다.",
              "data": null
            }
            """;

    static final String INTERNAL_SERVER_ERROR = """
            {
              "success": false,
              "code": "COMMON_500",
              "message": "서버 에러가 발생했습니다.",
              "data": null
            }
            """;
}
