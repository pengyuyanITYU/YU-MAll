package com.yu.ai.service;

import com.yu.ai.service.impl.AiChatExecutionRequest;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface AiChatExecutor {

    Flux<ServerSentEvent<String>> chat(AiChatExecutionRequest request);
}
