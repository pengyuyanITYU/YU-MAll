package com.yu.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yu.ai.config.YuAiProperties;
import com.yu.ai.service.AiChatExecutor;
import com.yu.ai.service.attachment.AiAttachmentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpringAiChatExecutorTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient.Builder chatClientBuilder;

    private SpringAiChatExecutor springAiChatExecutor;

    @BeforeEach
    void setUp() {
        YuAiProperties yuAiProperties = new YuAiProperties();
        yuAiProperties.setSystemPrompt("You are a mall AI assistant");
        springAiChatExecutor = new SpringAiChatExecutor(chatClientBuilder, yuAiProperties, OBJECT_MAPPER, "test-api-key");
    }

    @Test
    void buildUserMessage_shouldIncludePromptDocumentsAndImages() {
        AiChatExecutor.ChatRequest request = new AiChatExecutor.ChatRequest(
                "Summarize the key points",
                "qwen-vl-plus",
                0.2D,
                List.of(new Media(MimeTypeUtils.IMAGE_PNG, URI.create("https://cdn.test/image.png"))),
                List.of(new AiAttachmentResolver.DocumentAttachmentContent("report.pdf", "application/pdf", "sales amount is 2000"))
        );

        UserMessage userMessage = SpringAiChatExecutor.buildUserMessage(request);

        assertEquals(1, userMessage.getMedia().size());
        assertEquals(MimeTypeUtils.IMAGE_PNG, userMessage.getMedia().get(0).getMimeType());
        assertTrue(userMessage.getText().contains("Summarize the key points"));
        assertTrue(userMessage.getText().contains("report.pdf"));
        assertTrue(userMessage.getText().contains("sales amount is 2000"));
    }

    @Test
    void chat_shouldReturnTrimmedReply_whenFrameworkCallSucceeds() {
        when(chatClientBuilder.build()
                .prompt()
                .system(anyString())
                .messages(anyList())
                .options(any(OpenAiChatOptions.class))
                .stream()
                .content()).thenReturn(Flux.just("Hel", "lo"));

        List<ServerSentEvent<String>> events = springAiChatExecutor.chat(new AiChatExecutor.ChatRequest(
                "hello",
                "qwen3.5-plus",
                0.7D,
                List.of(),
                List.of()
        )).collectList().block();

        assertEquals(4, events.size());
        assertEquals("start", events.get(0).event());
        assertEquals("delta", events.get(1).event());
        assertEquals("delta", events.get(2).event());
        assertEquals("end", events.get(3).event());
        assertEquals("Hel", readJson(events.get(1).data()).get("content").asText());
        assertEquals("Hello", readJson(events.get(3).data()).get("reply").asText());
    }

    @Test
    void chat_shouldEmitErrorEvent_whenReplyBlank() {
        when(chatClientBuilder.build()
                .prompt()
                .system(anyString())
                .messages(anyList())
                .options(any(OpenAiChatOptions.class))
                .stream()
                .content()).thenReturn(Flux.just(" "));

        List<ServerSentEvent<String>> events = springAiChatExecutor.chat(new AiChatExecutor.ChatRequest(
                "hello",
                "qwen3.5-plus",
                0.7D,
                List.of(),
                List.of()
        )).collectList().block();

        assertEquals(2, events.size());
        assertEquals("start", events.get(0).event());
        assertEquals("error", events.get(1).event());
        assertEquals("AI response is empty", readJson(events.get(1).data()).get("message").asText());
    }

    @Test
    void chat_shouldEmitErrorEvent_whenFrameworkCallFails() {
        when(chatClientBuilder.build()
                .prompt()
                .system(anyString())
                .messages(anyList())
                .options(any(OpenAiChatOptions.class))
                .stream()
                .content()).thenReturn(Flux.error(new IllegalStateException("boom")));

        List<ServerSentEvent<String>> events = springAiChatExecutor.chat(
                new AiChatExecutor.ChatRequest("hello", "qwen3.5-plus", 0.7D, List.of(), List.of())
        ).collectList().block();

        assertEquals(2, events.size());
        assertEquals("start", events.get(0).event());
        assertEquals("error", events.get(1).event());
        assertEquals("AI call failed: boom", readJson(events.get(1).data()).get("message").asText());
    }

    @Test
    void chat_shouldEmitErrorEvent_whenApiKeyMissing() {
        YuAiProperties yuAiProperties = new YuAiProperties();
        SpringAiChatExecutor executor = new SpringAiChatExecutor(chatClientBuilder, yuAiProperties, OBJECT_MAPPER, "");

        List<ServerSentEvent<String>> events = executor.chat(
                new AiChatExecutor.ChatRequest("hello", "qwen3.5-plus", 0.7D, List.of(), List.of())
        ).collectList().block();

        assertEquals(1, events.size());
        assertEquals("error", events.get(0).event());
        assertEquals("YU_AI_API_KEY is not configured", readJson(events.get(0).data()).get("message").asText());
    }

    private JsonNode readJson(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}