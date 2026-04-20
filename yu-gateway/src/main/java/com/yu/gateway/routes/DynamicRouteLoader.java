package com.yu.gateway.routes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
@Component
@Slf4j
public class DynamicRouteLoader {

    private static final String DATA_ID = "gateway-routes.json";
    private static final String GROUP = "DEFAULT_GROUP";
    private static final int MAX_RETRIES = 20;
    private static final long RETRY_INTERVAL_MILLIS = 1500L;

    private final RouteDefinitionWriter routeDefinitionWriter;
    private final NacosConfigManager nacosConfigManager;
    private final Set<String> routeIds = new HashSet<>();
    private final AtomicBoolean listenerRegistered = new AtomicBoolean(false);

    @EventListener(ApplicationReadyEvent.class)
    public void initRouteConfigListener() {
        if (!listenerRegistered.compareAndSet(false, true)) {
            return;
        }
        registerRouteListenerWithRetry();
    }

    void registerRouteListenerWithRetry() {
        Listener listener = new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String config) {
                log.debug("gateway dynamic routes changed: {}", config);
                updateConfig(config);
            }
        };

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                String config = nacosConfigManager.getConfigService()
                        .getConfigAndSignListener(DATA_ID, GROUP, 5000, listener);
                if (StringUtils.hasText(config)) {
                    updateConfig(config);
                }
                log.info("gateway dynamic route listener registered, dataId={}, group={}", DATA_ID, GROUP);
                return;
            } catch (NacosException exception) {
                if (attempt == MAX_RETRIES) {
                    log.error("failed to register gateway dynamic route listener after {} attempts", attempt, exception);
                    return;
                }
                log.warn(
                        "gateway dynamic route listener registration attempt {} failed: {}",
                        attempt,
                        exception.getErrMsg()
                );
                sleepBeforeRetry();
            }
        }
    }

    private void updateConfig(String config) {
        List<RouteDefinition> routes = JSONUtil.toList(config, RouteDefinition.class);
        for (String routeId : routeIds) {
            routeDefinitionWriter.delete(Mono.just(routeId)).subscribe();
        }
        routeIds.clear();
        if (CollUtil.isEmpty(routes)) {
            return;
        }
        for (RouteDefinition routeDefinition : routes) {
            routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
            routeIds.add(routeDefinition.getId());
        }
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(RETRY_INTERVAL_MILLIS);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("gateway dynamic route listener retry interrupted", interruptedException);
        }
    }
}
