package com.yu.gateway.filter;

import com.yu.common.utils.ErrorMessageSanitizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class GatewayExceptionHandler implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }
        HttpStatus status = resolveStatus(ex);
        String message = resolveMessage(status);
        if (status.is5xxServerError()) {
            log.error("Gateway error on path {}", exchange.getRequest().getPath(), ex);
        } else {
            log.warn("Gateway handled status {} on path {} -> {}", status.value(), exchange.getRequest().getPath(), ex.getMessage());
        }
        return GatewayErrorResponseWriter.write(exchange.getResponse(), status, message);
    }

    private HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException responseStatusException) {
            return HttpStatus.valueOf(responseStatusException.getStatusCode().value());
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveMessage(HttpStatus status) {
        if (status == HttpStatus.UNAUTHORIZED) {
            return "登录已过期，请重新登录";
        }
        if (status == HttpStatus.NOT_FOUND) {
            return ErrorMessageSanitizer.sanitizeNotFoundMessage();
        }
        if (status == HttpStatus.SERVICE_UNAVAILABLE) {
            return "服务暂不可用，请稍后重试";
        }
        return "网关服务异常，请稍后重试";
    }
}
