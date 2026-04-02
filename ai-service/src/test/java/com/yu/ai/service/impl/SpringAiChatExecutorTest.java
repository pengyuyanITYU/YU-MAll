package com.yu.ai.service.impl;

import com.yu.ai.config.YuAiProperties;
import com.yu.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpringAiChatExecutorTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient.Builder chatClientBuilder;

    private SpringAiChatExecutor springAiChatExecutor;

    @BeforeEach
    void setUp() {
        YuAiProperties yuAiProperties = new YuAiProperties();
        yuAiProperties.setSystemPrompt("你是商城AI助手");
        springAiChatExecutor = new SpringAiChatExecutor(chatClientBuilder, yuAiProperties);
    }

    @Test
    void chat_shouldReturnTrimmedReply_whenFrameworkCallSucceeds() {
        when(chatClientBuilder.build()
                .prompt()
                .system(anyString())
                .user(anyString())
                .options(any(OpenAiChatOptions.class))
                .call()
                .content()).thenReturn("  已完成  ");

        String reply = springAiChatExecutor.chat("你好", "gpt-4o-mini", 0.7);

        assertEquals("已完成", reply);
    }

    @Test
    void chat_shouldThrowBusinessException_whenReplyBlank() {
        when(chatClientBuilder.build()
                .prompt()
                .system(anyString())
                .user(anyString())
                .options(any(OpenAiChatOptions.class))
                .call()
                .content()).thenReturn(" ");

        assertThrows(BusinessException.class, () -> springAiChatExecutor.chat("你好", "gpt-4o-mini", 0.7));
    }

    @Test
    void chat_shouldWrapFrameworkException_whenFrameworkCallFails() {
        when(chatClientBuilder.build()
                .prompt()
                .system(anyString())
                .user(anyString())
                .options(any(OpenAiChatOptions.class))
                .call()
                .content()).thenThrow(new IllegalStateException("boom"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> springAiChatExecutor.chat("你好", "gpt-4o-mini", 0.7));

        assertEquals("AI调用失败: boom", exception.getMessage());
    }
}