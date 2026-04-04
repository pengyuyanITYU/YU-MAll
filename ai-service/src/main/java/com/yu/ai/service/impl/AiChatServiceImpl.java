package com.yu.ai.service.impl;

import com.yu.ai.config.YuAiProperties;
import com.yu.ai.service.AiChatExecutor;
import com.yu.ai.service.IAiChatService;
import com.yu.ai.service.attachment.AiAttachmentResolver;
import com.yu.api.dto.AiChatRequestDTO;
import com.yu.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements IAiChatService {

    private final YuAiProperties yuAiProperties;
    private final AiChatExecutor aiChatExecutor;
    private final AiAttachmentResolver aiAttachmentResolver;

    @Override
    public Flux<ServerSentEvent<String>> chat(AiChatRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new BadRequestException("Request cannot be null");
        }
        if (!StringUtils.hasText(requestDTO.getMessage()) && CollectionUtils.isEmpty(requestDTO.getAttachments())) {
            throw new BadRequestException("Message and attachments cannot both be empty");
        }

        String model = StringUtils.hasText(requestDTO.getModel())
                ? requestDTO.getModel().trim()
                : yuAiProperties.getDefaultModel();
        Double temperature = requestDTO.getTemperature() == null
                ? yuAiProperties.getDefaultTemperature()
                : requestDTO.getTemperature();
        AiAttachmentResolver.ResolvedAttachments resolvedAttachments = aiAttachmentResolver.resolve(requestDTO.getAttachments());
        return aiChatExecutor.chat(new AiChatExecutor.ChatRequest(
                StringUtils.hasText(requestDTO.getMessage()) ? requestDTO.getMessage().trim() : "",
                model,
                temperature,
                resolvedAttachments.imageMediaList(),
                resolvedAttachments.documentContents()
        ));
    }
}