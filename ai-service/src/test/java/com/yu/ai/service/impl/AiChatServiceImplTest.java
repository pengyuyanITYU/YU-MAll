package com.yu.ai.service.impl;

import com.yu.ai.config.YuAiProperties;
import com.yu.ai.service.AiChatExecutor;
import com.yu.ai.service.attachment.AiAttachmentResolver;
import com.yu.ai.service.attachment.DocumentAttachmentContent;
import com.yu.ai.service.attachment.ResolvedAttachments;
import com.yu.api.dto.AiChatAttachmentDTO;
import com.yu.api.dto.AiChatRequestDTO;
import com.yu.common.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.content.Media;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiChatServiceImplTest {

    private final YuAiProperties yuAiProperties = new YuAiProperties();
    private final AiChatExecutor aiChatExecutor = mock(AiChatExecutor.class);
    private final AiAttachmentResolver aiAttachmentResolver = mock(AiAttachmentResolver.class);
    private AiChatServiceImpl aiChatService;

    @BeforeEach
    void setUp() {
        yuAiProperties.setDefaultModel("qwen3.5-plus");
        yuAiProperties.setDefaultTemperature(0.3D);
        aiChatService = new AiChatServiceImpl(yuAiProperties, aiChatExecutor, aiAttachmentResolver);
    }

    @Test
    void chat_shouldThrowBadRequestException_whenMessageAndAttachmentsEmpty() {
        AiChatRequestDTO requestDTO = new AiChatRequestDTO();
        requestDTO.setMessage(" ");

        assertThrows(BadRequestException.class, () -> aiChatService.chat(requestDTO));
    }

    @Test
    void chat_shouldUseDefaultModelAndResolvedAttachments() {
        AiChatRequestDTO requestDTO = new AiChatRequestDTO();
        requestDTO.setMessage("Summarize these attachments");
        requestDTO.setTemperature(0.1D);
        requestDTO.setAttachments(List.of(buildAttachment("https://cdn.test/demo.png", "demo.png", "image/png")));

        Media image = new Media(MimeTypeUtils.IMAGE_PNG, URI.create("https://cdn.test/demo.png"));
        ResolvedAttachments resolvedAttachments = new ResolvedAttachments(
                List.of(image),
                List.of(new DocumentAttachmentContent("spec.pdf", "application/pdf", "order count is 12"))
        );
        when(aiAttachmentResolver.resolve(requestDTO.getAttachments())).thenReturn(resolvedAttachments);
        when(aiChatExecutor.chat(argThat(request -> "Summarize these attachments".equals(request.message())
                && "qwen3.5-plus".equals(request.model())
                && Double.valueOf(0.1D).equals(request.temperature())
                && request.imageMediaList().size() == 1
                && request.documentContents().size() == 1
                && "order count is 12".equals(request.documentContents().get(0).content()))))
                .thenReturn(Flux.just(ServerSentEvent.builder("done").event("end").build()));

        List<ServerSentEvent<String>> events = aiChatService.chat(requestDTO).collectList().block();

        assertEquals(1, events.size());
        assertEquals("end", events.get(0).event());
        assertEquals("done", events.get(0).data());
        verify(aiAttachmentResolver).resolve(requestDTO.getAttachments());
    }

    @Test
    void chat_shouldAllowAttachmentOnlyRequest() {
        AiChatRequestDTO requestDTO = new AiChatRequestDTO();
        requestDTO.setAttachments(List.of(buildAttachment("https://cdn.test/report.xlsx", "report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")));

        ResolvedAttachments resolvedAttachments = new ResolvedAttachments(
                List.of(),
                List.of(new DocumentAttachmentContent("report.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "sales 100"))
        );
        when(aiAttachmentResolver.resolve(requestDTO.getAttachments())).thenReturn(resolvedAttachments);
        when(aiChatExecutor.chat(argThat(request -> "".equals(request.message())
                && request.documentContents().size() == 1)))
                .thenReturn(Flux.just(ServerSentEvent.builder("see attachment").event("end").build()));

        List<ServerSentEvent<String>> events = aiChatService.chat(requestDTO).collectList().block();

        assertEquals(1, events.size());
        assertEquals("end", events.get(0).event());
        assertEquals("see attachment", events.get(0).data());
    }

    private AiChatAttachmentDTO buildAttachment(String url, String fileName, String contentType) {
        AiChatAttachmentDTO attachmentDTO = new AiChatAttachmentDTO();
        attachmentDTO.setUrl(url);
        attachmentDTO.setFileName(fileName);
        attachmentDTO.setContentType(contentType);
        return attachmentDTO;
    }
}
