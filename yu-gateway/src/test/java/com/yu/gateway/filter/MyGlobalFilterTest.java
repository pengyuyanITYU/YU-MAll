package com.yu.gateway.filter;

import com.yu.gateway.config.AuthProperties;
import com.yu.gateway.config.JwtProperties;
import com.yu.gateway.utils.JwtTool;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class MyGlobalFilterTest {

    @Test
    void shouldNotThrowWhenExcludePathsIsNull() {
        AuthProperties authProperties = new AuthProperties();
        JwtProperties jwtProperties = new JwtProperties();
        JwtTool jwtTool = mock(JwtTool.class);
        MyGlobalFilter filter = new MyGlobalFilter(authProperties, jwtProperties, jwtTool);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/items/list").build()
        );
        GatewayFilterChain chain = serverWebExchange -> Mono.empty();

        assertDoesNotThrow(() -> filter.filter(exchange, chain).block());
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldAllowConfiguredExcludePathWithoutToken() {
        AuthProperties authProperties = new AuthProperties();
        authProperties.setExcludePaths(List.of("/users/login"));
        JwtProperties jwtProperties = new JwtProperties();
        JwtTool jwtTool = mock(JwtTool.class);
        MyGlobalFilter filter = new MyGlobalFilter(authProperties, jwtProperties, jwtTool);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/users/login").build()
        );
        GatewayFilterChain chain = serverWebExchange -> Mono.empty();

        filter.filter(exchange, chain).block();

        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldRejectProtectedPathWithoutToken() {
        AuthProperties authProperties = new AuthProperties();
        authProperties.setExcludePaths(List.of("/users/login"));
        JwtProperties jwtProperties = new JwtProperties();
        JwtTool jwtTool = mock(JwtTool.class);
        MyGlobalFilter filter = new MyGlobalFilter(authProperties, jwtProperties, jwtTool);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/orders/1").build()
        );
        GatewayFilterChain chain = serverWebExchange -> Mono.empty();

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }
}
