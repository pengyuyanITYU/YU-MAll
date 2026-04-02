package com.yu.ai.service.impl;

import com.yu.common.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

final class OpenAiChatResponseParser {

    private OpenAiChatResponseParser() {
    }

    static String parseReply(Map<String, Object> responseMap) {
        Object choicesObj = responseMap.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            throw new BusinessException("AI响应格式异常：choices为空");
        }
        Object firstChoice = choices.get(0);
        if (!(firstChoice instanceof Map<?, ?> choiceMap)) {
            throw new BusinessException("AI响应格式异常：choice结构错误");
        }
        Object messageObj = choiceMap.get("message");
        if (!(messageObj instanceof Map<?, ?> messageMap)) {
            throw new BusinessException("AI响应格式异常：message结构错误");
        }
        Object contentObj = messageMap.get("content");
        String content = contentObj == null ? null : contentObj.toString();
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("AI响应格式异常：content为空");
        }
        return content.trim();
    }
}
