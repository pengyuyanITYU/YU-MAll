package com.yu.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yu.common.domain.AjaxResult;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

final class GatewayErrorResponseWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private GatewayErrorResponseWriter() {
    }

    static Mono<Void> write(ServerHttpResponse response, HttpStatus status, String message) {
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = response.bufferFactory().wrap(toBody(status, message));
        return response.writeWith(Mono.just(buffer));
    }

    private static byte[] toBody(HttpStatus status, String message) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(AjaxResult.error(status.value(), message));
        } catch (JsonProcessingException e) {
            String fallback = "{\"code\":" + status.value() + ",\"msg\":\"" + message + "\",\"data\":null}";
            return fallback.getBytes(StandardCharsets.UTF_8);
        }
    }
}
