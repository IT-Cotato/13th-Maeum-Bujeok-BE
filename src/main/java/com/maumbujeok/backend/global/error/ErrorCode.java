// src/main/java/com/maumbujeok/backend/global/error/ErrorCode.java
package com.maumbujeok.backend.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INVALID_DIARY_CURSOR(HttpStatus.BAD_REQUEST, "DIARY_CURSOR_400", "Invalid diary cursor."),

    INVALID_UPLOAD_REQUEST(HttpStatus.BAD_REQUEST, "UPLOAD_400", "Invalid upload request."),
    UPLOAD_NOT_FOUND(HttpStatus.NOT_FOUND, "UPLOAD_404", "Upload not found."),
    UPLOAD_STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "UPLOAD_500", "Object storage operation failed."),
    
    // Auth
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_001", "議댁옱?섏? ?딅뒗 ?ъ슜?먯엯?덈떎."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH_002", "鍮꾨?踰덊샇媛 ?쇱튂?섏? ?딆뒿?덈떎."),
    DUPLICATE_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "AUTH_004", "?대? 媛?낅맂 ?꾪솕踰덊샇?낅땲??"),
    ALREADY_REGISTERED_PHONE(HttpStatus.CONFLICT, "AUTH_009", "?대? 媛?낅맂 ?꾪솕踰덊샇?낅땲??"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_010", "議댁옱?섏? ?딅뒗 ?뚯썝?낅땲??"),
    INVALID_SMS_PURPOSE(HttpStatus.BAD_REQUEST, "AUTH_012", "?щ컮瑜댁? ?딆? SMS ?붿껌 紐⑹쟻?낅땲??"),

    // Member
    INVALID_NOTIFICATION_SETTINGS(HttpStatus.BAD_REQUEST, "MEMBER_001", "?뚮┝ ?ㅼ젙 媛믪씠 ?щ컮瑜댁? ?딆뒿?덈떎."),
    INVALID_NOTIFICATION_DAYS(HttpStatus.BAD_REQUEST, "MEMBER_002", "?붿씪蹂??뚮┝ ?ㅼ젙 媛믪씠 ?щ컮瑜댁? ?딆뒿?덈떎."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_005", "?좏슚?섏? ?딄굅??留뚮즺??Refresh Token?낅땲??"),
    PHONE_NUMBER_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_006", "?뚯썝 ?뺣낫???꾪솕踰덊샇? ?쇱튂?섏? ?딆뒿?덈떎."),
    SOCIAL_USER_CANNOT_RESET_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH_008", "?뚯뀥 濡쒓렇??媛?낆옄??鍮꾨?踰덊샇瑜??ъ꽕?뺥븷 ???놁뒿?덈떎."),
    SOCIAL_USER_MUST_LOGIN_WITH_OAUTH(HttpStatus.BAD_REQUEST, "AUTH_011", "?뚯뀥 濡쒓렇?몄쑝濡?媛?낅맂 怨꾩젙?낅땲?? 援ш? 濡쒓렇?몄쓣 ?댁슜??二쇱꽭??"),

    // Diary
    INVALID_DIARY_REQUEST(HttpStatus.BAD_REQUEST, "DIARY_400", "?쇨린 ?붿껌???щ컮瑜댁? ?딆뒿?덈떎."),
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "DIARY_404", "?쇨린瑜?李얠쓣 ???놁뒿?덈떎."),

    // Burning
    INVALID_BURNING_REQUEST(HttpStatus.BAD_REQUEST, "BURN_400", "Invalid burning request."),
    BURNING_NOT_FOUND(HttpStatus.NOT_FOUND, "BURN_404", "Burning record not found."),
    DIARY_ALREADY_BURNED(HttpStatus.CONFLICT, "BURN_409", "Diary has already been burned."),

    // Report
    INVALID_REPORT_REQUEST(HttpStatus.BAD_REQUEST, "REPORT_400", "由ы룷???붿껌???щ컮瑜댁? ?딆뒿?덈떎."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_404", "由ы룷?몃? 李얠쓣 ???놁뒿?덈떎."),
    FLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "FLOW_001", "?ㅼ쓬 二??먮쫫??李얠쓣 ???놁뒿?덈떎."),
    
    // SMS Auth
    SMS_CODE_NOT_FOUND(HttpStatus.BAD_REQUEST, "SMS_001", "?몄쬆踰덊샇媛 議댁옱?섏? ?딄굅???쇱튂?섏? ?딆뒿?덈떎."),
    SMS_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "SMS_002", "留뚮즺???몄쬆踰덊샇?낅땲??"),
    SMS_CODE_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "SMS_003", "?꾪솕踰덊샇 ?몄쬆???꾨즺?섏? ?딆븯?듬땲??"),
    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SMS_004", "?몄쬆踰덊샇 ?꾩넚???ㅽ뙣?덉뒿?덈떎."),
    
    // Common
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "COMMON_403", "沅뚰븳???놁뒿?덈떎."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "?쒕쾭 ?먮윭媛 諛쒖깮?덉뒿?덈떎.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}


