package com.yu.gateway.routes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
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
    private final ApplicationEventPublisher applicationEventPublisher;
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

    void updateConfig(String config) {
        List<RouteDefinition> routes = parseRouteDefinitions(config);
        for (String routeId : routeIds) {
            routeDefinitionWriter.delete(Mono.just(routeId)).block();
        }
        routeIds.clear();
        if (CollUtil.isNotEmpty(routes)) {
            for (RouteDefinition routeDefinition : routes) {
                routeDefinitionWriter.save(Mono.just(routeDefinition)).block();
                routeIds.add(routeDefinition.getId());
            }
        }
        applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
        log.info("gateway dynamic routes refreshed, count={}", routeIds.size());
    }

    static List<RouteDefinition> parseRouteDefinitions(String config) {
        if (!StringUtils.hasText(config)) {
            return Collections.emptyList();
        }
        Object json = JSONUtil.parse(config);
        if (json instanceof JSONArray jsonArray) {
            return toRouteDefinitions(jsonArray);
        }
        if (json instanceof JSONObject jsonObject) {
            Object routes = jsonObject.get("routes");
            if (routes == null) {
                routes = jsonObject.get("data");
            }
            if (routes != null) {
                return toRouteDefinitions(JSONUtil.parseArray(routes));
            }
            if (jsonObject.containsKey("id")) {
                return Collections.singletonList(toRouteDefinition(jsonObject));
            }
        }
        return Collections.emptyList();
    }

    private static List<RouteDefinition> toRouteDefinitions(JSONArray jsonArray) {
        List<RouteDefinition> routes = new ArrayList<>(jsonArray.size());
        for (Object item : jsonArray) {
            if (item instanceof JSONObject jsonObject) {
                routes.add(toRouteDefinition(jsonObject));
            }
        }
        return routes;
    }

    private static RouteDefinition toRouteDefinition(JSONObject jsonObject) {
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(jsonObject.getStr("id"));
        String uri = jsonObject.getStr("uri");
        if (StringUtils.hasText(uri)) {
            routeDefinition.setUri(URI.create(uri));
        }
        Integer order = jsonObject.getInt("order");
        if (order != null) {
            routeDefinition.setOrder(order);
        }
        routeDefinition.setPredicates(toPredicateDefinitions(jsonObject.getJSONArray("predicates")));
        routeDefinition.setFilters(toFilterDefinitions(jsonObject.getJSONArray("filters")));
        JSONObject metadata = jsonObject.getJSONObject("metadata");
        if (metadata != null) {
            routeDefinition.setMetadata(metadata);
        }
        return routeDefinition;
    }

    private static List<PredicateDefinition> toPredicateDefinitions(JSONArray predicates) {
        if (predicates == null || predicates.isEmpty()) {
            return Collections.emptyList();
        }
        List<PredicateDefinition> definitions = new ArrayList<>(predicates.size());
        for (Object predicate : predicates) {
            if (predicate instanceof CharSequence predicateText) {
                definitions.add(new PredicateDefinition(predicateText.toString()));
            } else if (predicate instanceof JSONObject predicateObject) {
                definitions.add(toPredicateDefinition(predicateObject));
            }
        }
        return definitions;
    }

    private static PredicateDefinition toPredicateDefinition(JSONObject predicateObject) {
        PredicateDefinition definition = new PredicateDefinition();
        definition.setName(predicateObject.getStr("name"));
        JSONObject args = predicateObject.getJSONObject("args");
        if (args != null) {
            definition.setArgs(toStringMap(args));
        }
        return definition;
    }

    private static List<FilterDefinition> toFilterDefinitions(JSONArray filters) {
        if (filters == null || filters.isEmpty()) {
            return Collections.emptyList();
        }
        List<FilterDefinition> definitions = new ArrayList<>(filters.size());
        for (Object filter : filters) {
            if (filter instanceof CharSequence filterText) {
                definitions.add(new FilterDefinition(filterText.toString()));
            } else if (filter instanceof JSONObject filterObject) {
                definitions.add(toFilterDefinition(filterObject));
            }
        }
        return definitions;
    }

    private static FilterDefinition toFilterDefinition(JSONObject filterObject) {
        FilterDefinition definition = new FilterDefinition();
        definition.setName(filterObject.getStr("name"));
        JSONObject args = filterObject.getJSONObject("args");
        if (args != null) {
            definition.setArgs(toStringMap(args));
        }
        return definition;
    }

    private static Map<String, String> toStringMap(JSONObject jsonObject) {
        Map<String, String> args = new LinkedHashMap<>();
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            args.put(key, value == null ? null : String.valueOf(value));
        }
        return args;
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
