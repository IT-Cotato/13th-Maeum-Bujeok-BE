package com.maumbujeok.backend.domain.auth.service;

import com.maumbujeok.backend.domain.auth.domain.SmsAuthCode;
import com.maumbujeok.backend.domain.auth.repository.SmsAuthCodeRepository;
import com.maumbujeok.backend.global.error.CustomException;
import com.maumbujeok.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final SmsAuthCodeRepository smsAuthCodeRepository;

    // SMS 인증코드 전송 Mock API
    @Transactional
    public void sendVerificationCode(String phoneNumber) {
        // 6자리 랜덤 인증코드 생성
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        
        // SMS MOCK 발송 로그 출력
        log.info("[SMS MOCK] To: {}, Code: {}", phoneNumber, code);

        // 만료 기한은 3분
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(3);

        SmsAuthCode smsAuthCode = SmsAuthCode.builder()
                .phoneNumber(phoneNumber)
                .code(code)
                .expiredAt(expiredAt)
                .build();

        smsAuthCodeRepository.save(smsAuthCode);
    }

    // SMS 인증코드 검증 API (3분 이내 유효성 확인)
    @Transactional
    public void verifyCode(String phoneNumber, String code) {
        // 해당 전화번호로 가장 최근에 발송된 인증번호 조회
        SmsAuthCode smsAuthCode = smsAuthCodeRepository.findTopByPhoneNumberOrderByCreatedAtDesc(phoneNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.SMS_CODE_NOT_FOUND));

        // 입력한 인증코드가 일치하는지 비교
        if (!smsAuthCode.getCode().equals(code)) {
            throw new CustomException(ErrorCode.SMS_CODE_NOT_FOUND);
        }

        // 인증코드 만료 기한 체크 (3분)
        if (smsAuthCode.isExpired()) {
            throw new CustomException(ErrorCode.SMS_CODE_EXPIRED);
        }

        // 이미 사용/인증 완료된 코드가 아닐 경우에만 검증 완료 처리
        if (smsAuthCode.getIsVerified()) {
            throw new CustomException(ErrorCode.SMS_CODE_NOT_FOUND);
        }

        smsAuthCode.verify(); // isVerified = true
    }
}
