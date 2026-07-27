package com.maumbujeok.backend.domain.auth.exception;

import com.maumbujeok.backend.global.error.CustomException;
import com.maumbujeok.backend.global.error.ErrorCode;

public class SmsSendFailedException extends CustomException {
    public SmsSendFailedException() {
        super(ErrorCode.SMS_SEND_FAILED);
    }
}
