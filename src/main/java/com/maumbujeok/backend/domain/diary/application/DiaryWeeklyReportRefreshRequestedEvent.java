package com.maumbujeok.backend.domain.diary.application;

import java.time.LocalDate;

public record DiaryWeeklyReportRefreshRequestedEvent(String memberPhoneNumber, LocalDate diaryDate) {
}
