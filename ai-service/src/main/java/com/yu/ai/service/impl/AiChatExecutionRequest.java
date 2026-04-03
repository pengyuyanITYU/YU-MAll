package com.yu.ai.service.impl;

import com.yu.ai.service.attachment.DocumentAttachmentContent;
import org.springframework.ai.content.Media;

import java.util.List;

public record AiChatExecutionRequest(
        String message,
        String model,
        Double temperature,
        List<Media> imageMediaList,
        List<DocumentAttachmentContent> documentContents
) {

    public AiChatExecutionRequest {
        message = message == null ? "" : message;
        imageMediaList = imageMediaList == null ? List.of() : List.copyOf(imageMediaList);
        documentContents = documentContents == null ? List.of() : List.copyOf(documentContents);
    }
}