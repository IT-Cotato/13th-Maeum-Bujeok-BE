package com.maumbujeok.backend.domain.diary.repository;

import com.maumbujeok.backend.global.ai.emotion.ReportEmotion;

public interface DiaryEmotionCountProjection {
    ReportEmotion getEmotion();

    long getCount();
}
