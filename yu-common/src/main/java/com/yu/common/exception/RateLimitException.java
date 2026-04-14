package com.yu.common.exception;

import com.yu.common.constant.HttpStatus;

public class RateLimitException extends CommonException {

    public RateLimitException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }

    public RateLimitException(String message, Throwable cause) {
        super(message, cause, HttpStatus.TOO_MANY_REQUESTS);
    }
}
