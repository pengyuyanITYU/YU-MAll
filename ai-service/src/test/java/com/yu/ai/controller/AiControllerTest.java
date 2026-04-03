package com.yu.ai.controller;

import com.yu.ai.service.IAiChatService;
import com.yu.api.dto.AiChatRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(AiController.class)
class AiControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private IAiChatService aiChatService;

    @Test
    void chat_shouldReturnTextEventStream() {
        when(aiChatService.chat(any(AiChatRequestDTO.class)))
                .thenReturn(Flux.just(ServerSentEvent.builder("{\"content\":\"hello\"}").event("delta").build()));

        webTestClient.post()
                .uri("/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue("{\"message\":\"hello\"}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBody(String.class)
                .value(body -> {
                    org.junit.jupiter.api.Assertions.assertTrue(body.contains("event:delta"));
                    org.junit.jupiter.api.Assertions.assertTrue(body.contains("{\"content\":\"hello\"}"));
                });
    }
}
