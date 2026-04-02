package com.yu.ai.service.impl;

import com.yu.ai.config.YuAiProperties;
import com.yu.ai.service.AiChatExecutor;
import com.yu.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class SpringAiChatExecutor implements AiChatExecutor {

    private static final String PLACEHOLDER_API_KEY = "sk-local-placeholder";

    private final ChatClient.Builder chatClientBuilder;
    private final YuAiProperties yuAiProperties;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Override
    public String chat(String message, String model, Double temperature) {
        if (!StringUtils.hasText(apiKey) || PLACEHOLDER_API_KEY.equals(apiKey)) {
            throw new BusinessException("未配置 YU_AI_API_KEY");
        }
        try {
            String reply = chatClientBuilder.build()
                    .prompt()
                    .system(yuAiProperties.getSystemPrompt())
                    .user(message)
                    .options(OpenAiChatOptions.builder()
                            .model(model)
                            .temperature(temperature)
                            .build())
                    .call()
                    .content();
            if (!StringUtils.hasText(reply)) {
                throw new BusinessException("AI响应为空");
            }
            return reply.trim();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("AI调用失败: " + e.getMessage());
        }
    }
}