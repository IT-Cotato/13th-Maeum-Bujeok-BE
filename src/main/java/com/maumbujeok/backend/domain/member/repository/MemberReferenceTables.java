package com.maumbujeok.backend.domain.member.repository;

import java.util.List;

public final class MemberReferenceTables {

    public static final String MEMBER_SAJU_PROFILES = "member_saju_profiles";
    public static final String MEMBER_NOTIFICATION_SETTINGS = "member_notification_settings";
    public static final String DIARY_UPLOADS = "diary_uploads";
    public static final String DIARIES = "diaries";
    public static final String TALISMANS = "talismans";
    public static final String BURNINGS = "burnings";
    public static final String EMOTION_REPORTS = "emotion_reports";
    public static final String NEXT_WEEK_FLOWS = "next_week_flows";
    public static final String HOME_SUMMARIES = "home_summaries";
    public static final String SAJU_ANALYSES = "saju_analyses";

    public static final List<String> MEMBER_REFERENCE_TABLES = List.of(
            MEMBER_SAJU_PROFILES,
            MEMBER_NOTIFICATION_SETTINGS,
            DIARY_UPLOADS,
            DIARIES,
            TALISMANS,
            BURNINGS,
            NEXT_WEEK_FLOWS,
            EMOTION_REPORTS,
            HOME_SUMMARIES,
            SAJU_ANALYSES
    );

    private MemberReferenceTables() {
    }
}
