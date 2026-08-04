package com.maumbujeok.backend.domain.auth.service;

import com.maumbujeok.backend.domain.auth.domain.SmsAuthCode;
import com.maumbujeok.backend.domain.auth.dto.SmsPurpose;
import com.maumbujeok.backend.domain.auth.exception.SmsSendFailedException;
import com.maumbujeok.backend.domain.auth.repository.SmsAuthCodeRepository;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.global.error.CustomException;
import com.maumbujeok.backend.global.error.ErrorCode;
import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final SmsAuthCodeRepository smsAuthCodeRepository;
    private final MemberRepository memberRepository;

    @Value("${coolsms.api-key}")
    private String apiKey;

    @Value("${coolsms.api-secret}")
    private String apiSecret;

    @Value("${coolsms.sender-number}")
    private String senderNumber;

    private DefaultMessageService messageService;

    @PostConstruct
    public void init() {
        this.messageService = SolapiClient.INSTANCE.createInstance(apiKey, apiSecret);
    }

    // SMS 인증코드 전송 API
    @Transactional
    public void sendVerificationCode(String phoneNumber, SmsPurpose purpose) {
        // 1. 유효성 검증 분기
        if (purpose == SmsPurpose.SIGNUP) {
            if (memberRepository.existsByPhoneNumber(phoneNumber)) {
                throw new CustomException(ErrorCode.ALREADY_REGISTERED_PHONE);
            }
        } else if (purpose == SmsPurpose.PASSWORD_RESET) {
            if (!memberRepository.existsByPhoneNumber(phoneNumber)) {
                throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
            }
        } else {
            throw new CustomException(ErrorCode.INVALID_SMS_PURPOSE);
        }

        // 2. 테스트 전용 전화번호 (Bypass) 로직
        boolean isBypass = "01099999999".equals(phoneNumber) || "010-9999-9999".equals(phoneNumber);
        String code;
        if (isBypass) {
            code = "123456";
            log.info("[SMS BYPASS] Mock sent verification code 123456 to test number: {}", phoneNumber);
        } else {
            // 6자리 랜덤 인증코드 생성
            code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
            
            // SMS 실제 발송 처리
            Message message = new Message();
            message.setFrom(senderNumber);
            message.setTo(phoneNumber);
            message.setText(String.format("[마음부적] 인증번호는 [%s] 입니다.", code));

            try {
                messageService.send(message, null);
                log.info("[SMS] Sent verification code to: {}", phoneNumber);
            } catch (Exception e) {
                log.error("[SMS] Failed to send verification code to: {}, Error: {}", phoneNumber, e.getMessage(), e);
                throw new SmsSendFailedException();
            }
        }

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
