package com.yu.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yu.ai.config.YuAiProperties;
import com.yu.ai.service.AiChatExecutor;
import com.yu.ai.service.attachment.DocumentAttachmentContent;
import com.yu.common.exception.BusinessException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class SpringAiChatExecutor implements AiChatExecutor {

    private static final String PLACEHOLDER_API_KEY = "sk-local-placeholder";
    private static final String DEFAULT_ATTACHMENT_PROMPT = "Please analyze the uploaded attachments and answer the user.";

    private final ChatClient.Builder chatClientBuilder;
    private final YuAiProperties yuAiProperties;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public SpringAiChatExecutor(ChatClient.Builder chatClientBuilder,
                                YuAiProperties yuAiProperties,
                                ObjectMapper objectMapper,
                                @Value("${spring.ai.openai.api-key:}") String apiKey) {
        this.chatClientBuilder = chatClientBuilder;
        this.yuAiProperties = yuAiProperties;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }

    @Override
    public Flux<ServerSentEvent<String>> chat(AiChatExecutionRequest request) {
        if (!StringUtils.hasText(apiKey) || PLACEHOLDER_API_KEY.equals(apiKey)) {
            return Flux.just(buildErrorEvent("YU_AI_API_KEY is not configured"));
        }
        String messageId = UUID.randomUUID().toString();
        StringBuilder replyBuilder = new StringBuilder();
        try {
            Flux<ServerSentEvent<String>> deltaFlux = chatClientBuilder.build()
                    .prompt()
                    .system(yuAiProperties.getSystemPrompt())
                    .messages(List.of(buildUserMessage(request)))
                    .options(OpenAiChatOptions.builder()
                            .model(request.model())
                            .temperature(request.temperature())
                            .build())
                    .stream()
                    .content()
                    .handle((chunk, sink) -> {
                        if (StringUtils.hasText(chunk)) {
                            replyBuilder.append(chunk);
                            sink.next(buildEvent("delta", Map.of("content", chunk)));
                        }
                    });
            return Flux.concat(
                    Flux.just(buildEvent("start", startPayload(messageId, request.model()))),
                    deltaFlux,
                    Flux.defer(() -> {
                        String reply = replyBuilder.toString().trim();
                        if (!StringUtils.hasText(reply)) {
                            return Flux.just(buildErrorEvent("AI response is empty"));
                        }
                        return Flux.just(buildEvent("end", endPayload(messageId, request.model(), reply)));
                    })
            ).onErrorResume(BusinessException.class, e -> Flux.just(buildErrorEvent(e.getMessage())))
                    .onErrorResume(Exception.class, e -> Flux.just(buildErrorEvent("AI call failed: " + e.getMessage())));
        } catch (BusinessException e) {
            return Flux.just(buildErrorEvent(e.getMessage()));
        } catch (Exception e) {
            return Flux.just(buildErrorEvent("AI call failed: " + e.getMessage()));
        }
    }

    static UserMessage buildUserMessage(AiChatExecutionRequest request) {
        String text = buildUserText(request);
        return UserMessage.builder()
                .text(text)
                .media(request.imageMediaList())
                .build();
    }

    private static String buildUserText(AiChatExecutionRequest request) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(request.message())) {
            builder.append(request.message().trim());
        }
        if (!request.documentContents().isEmpty()) {
            if (!builder.isEmpty()) {
                builder.append("\n\n");
            }
            builder.append("Uploaded document contents:");
            for (DocumentAttachmentContent documentContent : request.documentContents()) {
                builder.append("\n\n[Document] ")
                        .append(documentContent.fileName())
                        .append("\n")
                        .append(documentContent.content());
            }
        }
        if (!builder.isEmpty()) {
            return builder.toString();
        }
        return DEFAULT_ATTACHMENT_PROMPT;
    }

    private Map<String, Object> startPayload(String messageId, String model) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("messageId", messageId);
        payload.put("model", model);
        return payload;
    }

    private Map<String, Object> endPayload(String messageId, String model, String reply) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("messageId", messageId);
        payload.put("model", model);
        payload.put("reply", reply);
        return payload;
    }

    private ServerSentEvent<String> buildErrorEvent(String message) {
        return buildEvent("error", Map.of("message", message));
    }

    private ServerSentEvent<String> buildEvent(String eventName, Map<String, Object> payload) {
        return ServerSentEvent.<String>builder(serialize(payload))
                .event(eventName)
                .build();
    }

    private String serialize(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize SSE payload", e);
        }
    }
}
