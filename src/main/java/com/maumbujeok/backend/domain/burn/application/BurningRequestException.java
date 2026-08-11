package com.maumbujeok.backend.domain.burn.application;
import com.maumbujeok.backend.global.error.ErrorCode;
public class BurningRequestException extends RuntimeException { private final ErrorCode errorCode; public BurningRequestException(ErrorCode code, String message){super(message); this.errorCode=code;} public ErrorCode getErrorCode(){return errorCode;} }
