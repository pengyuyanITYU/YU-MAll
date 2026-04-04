package com.yu.ai.service;

import com.yu.ai.service.attachment.AiAttachmentResolver;
import org.springframework.ai.content.Media;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiChatExecutor {

    Flux<ServerSentEvent<String>> chat(ChatRequest request);

    record ChatRequest(
            String message,
            String model,
            Double temperature,
            List<Media> imageMediaList,
            List<AiAttachmentResolver.DocumentAttachmentContent> documentContents
    ) {

        public ChatRequest {
            message = message == null ? "" : message;
            imageMediaList = imageMediaList == null ? List.of() : List.copyOf(imageMediaList);
            documentContents = documentContents == null ? List.of() : List.copyOf(documentContents);
        }
    }
}