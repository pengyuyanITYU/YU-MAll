package com.yu.ai.service.impl;

import com.yu.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenAiChatResponseParserTest {

    @Test
    void parseReply_shouldReturnContent_whenResponseIsValid() {
        Map<String, Object> response = Map.of(
                "choices", List.of(
                        Map.of("message", Map.of("content", "hello"))
                )
        );

        String reply = OpenAiChatResponseParser.parseReply(response);

        assertEquals("hello", reply);
    }

    @Test
    void parseReply_shouldThrow_whenChoicesEmpty() {
        Map<String, Object> response = Map.of("choices", List.of());

        assertThrows(BusinessException.class, () -> OpenAiChatResponseParser.parseReply(response));
    }
}
