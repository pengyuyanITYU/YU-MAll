package com.yu.ai.service;

import com.yu.api.dto.AiChatRequestDTO;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface IAiChatService {

    Flux<ServerSentEvent<String>> chat(AiChatRequestDTO requestDTO);
}
