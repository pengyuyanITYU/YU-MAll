package com.yu.common.exception;

import com.yu.common.constant.HttpStatus;

public class CaptchaRequiredException extends CommonException {

    public CaptchaRequiredException(String message) {
        super(message, HttpStatus.PRECONDITION_REQUIRED);
    }

    public CaptchaRequiredException(String message, Throwable cause) {
        super(message, cause, HttpStatus.PRECONDITION_REQUIRED);
    }
}
