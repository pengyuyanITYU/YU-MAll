package com.yu.ai.controller;

import com.yu.ai.service.IAiChatService;
import com.yu.api.dto.AiChatRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final IAiChatService aiChatService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@Valid @RequestBody AiChatRequestDTO requestDTO) {
        return aiChatService.chat(requestDTO);
    }
}
