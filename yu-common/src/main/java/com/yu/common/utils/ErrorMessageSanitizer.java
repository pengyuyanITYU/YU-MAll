package com.yu.common.utils;

import java.util.regex.Pattern;

public final class ErrorMessageSanitizer {

    public static final String SYSTEM_ERROR_MESSAGE = "系统异常，请稍后重试";
    public static final String OPERATION_ERROR_MESSAGE = "操作失败，请稍后重试";
    public static final String DATA_ACCESS_ERROR_MESSAGE = "数据操作失败，请稍后重试";
    public static final String REQUEST_NOT_FOUND_MESSAGE = "请求地址不存在";

    private static final Pattern TECHNICAL_MESSAGE_PATTERN = Pattern.compile(
            "(?i)(exception|error|sql|mybatis|jdbc|redis|feign|gateway|trace|stack|org\\.|java\\.|"
                    + "nested exception|nullpointer|no static resource|class path|syntax|timeout|refused|"
                    + "failed to|<html|<!doctype|select\\s+.+from|insert\\s+into|update\\s+.+set|delete\\s+from)"
    );

    private ErrorMessageSanitizer() {
    }

    public static String sanitizeBusinessMessage(String message) {
        return sanitize(message, OPERATION_ERROR_MESSAGE);
    }

    public static String sanitizeDataAccessMessage(String message) {
        return sanitize(message, DATA_ACCESS_ERROR_MESSAGE);
    }

    public static String sanitizeValidationMessage(String message) {
        return sanitize(message, OPERATION_ERROR_MESSAGE);
    }

    public static String sanitizeSystemMessage() {
        return SYSTEM_ERROR_MESSAGE;
    }

    public static String sanitizeNotFoundMessage() {
        return REQUEST_NOT_FOUND_MESSAGE;
    }

    private static String sanitize(String message, String fallback) {
        if (message == null || message.isBlank()) {
            return fallback;
        }
        String trimmed = message.trim();
        if (TECHNICAL_MESSAGE_PATTERN.matcher(trimmed).find()) {
            return fallback;
        }
        return trimmed;
    }
}
