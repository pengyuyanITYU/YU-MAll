package com.yu.gateway.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.AntPathMatcher;
import com.yu.gateway.config.AuthProperties;
import com.yu.gateway.config.JwtProperties;
import com.yu.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;


@EnableConfigurationProperties(AuthProperties.class)
@Component
@RequiredArgsConstructor
@Slf4j
public class MyGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;

    private final JwtProperties jwtProperties;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private final JwtTool jwtTool;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.debug("Enter MyGlobalFilter");
        ServerHttpRequest request = exchange.getRequest();
        if(isExcludePath(request.getPath().value())){
                log.info("Skip auth for excluded path");
                return chain.filter(exchange);
            }
        HttpHeaders headers = request.getHeaders();
        List<String> list = headers.get("Authorization");
        String token = null;
        if(!CollUtil.isEmpty(list)){
            token = list.get(0);
            // Strip Bearer prefix
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
        }
        Long userId = null;
        try{
            userId = jwtTool.parseToken(token);
        }catch(Exception e){
            log.error("Token validation failed", e);
            return GatewayErrorResponseWriter.write(exchange.getResponse(), HttpStatus.UNAUTHORIZED, "登录已过期，请重新登录");
        }
        log.info("Authenticated user {}", userId);
        String userInfo = userId.toString();
        ServerWebExchange build = exchange.mutate().request(u -> u.header("user-info", userInfo)).build();
        return chain.filter(build);
    }

    private boolean isExcludePath(String path){
        List<String> excludePaths = authProperties.getExcludePaths();
        if (CollUtil.isEmpty(excludePaths)) {
            return false;
        }
        for (String exclude : excludePaths) {
            if(antPathMatcher.match(exclude, path)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
