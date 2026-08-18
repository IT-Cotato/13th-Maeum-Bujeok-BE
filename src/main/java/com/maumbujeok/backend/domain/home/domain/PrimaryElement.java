package com.maumbujeok.backend.domain.home.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PrimaryElement {
    WOOD("목(木)", "새로운 시작과 성장의 기운"),
    FIRE("화(火)", "열정과 활력의 기운"),
    EARTH("토(土)", "안정과 포용의 기운"),
    METAL("금(金)", "절제와 결단의 기운"),
    WATER("수(水)", "지혜와 유연함의 기운");

    private final String title;
    private final String description;
}
