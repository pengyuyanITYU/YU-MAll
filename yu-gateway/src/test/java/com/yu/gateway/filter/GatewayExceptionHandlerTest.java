package com.yu.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewayExceptionHandlerTest {

    @Test
    void shouldReturnSafeJsonForNotFoundException() {
        GatewayExceptionHandler handler = new GatewayExceptionHandler();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/missing-path").build()
        );

        handler.handle(exchange, new ResponseStatusException(HttpStatus.NOT_FOUND, "No matching route"))
                .block();

        assertEquals(HttpStatus.NOT_FOUND, exchange.getResponse().getStatusCode());
        assertEquals("application/json", exchange.getResponse().getHeaders().getFirst("Content-Type"));
        String body = exchange.getResponse().getBodyAsString().block();
        assertTrue(body.contains("\"code\":404"));
        assertTrue(body.contains("\"msg\":\"请求地址不存在\""));
    }

    @Test
    void shouldReturnSafeJsonForUnexpectedException() {
        GatewayExceptionHandler handler = new GatewayExceptionHandler();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/orders/1").build()
        );

        handler.handle(exchange, new RuntimeException("java.lang.NullPointerException: gateway failed"))
                .block();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());
        assertEquals("application/json", exchange.getResponse().getHeaders().getFirst("Content-Type"));
        String body = exchange.getResponse().getBodyAsString().block();
        assertTrue(body.contains("\"code\":500"));
        assertTrue(body.contains("\"msg\":\"网关服务异常，请稍后重试\""));
    }
}
