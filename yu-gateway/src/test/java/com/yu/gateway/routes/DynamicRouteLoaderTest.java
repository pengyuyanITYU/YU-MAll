package com.yu.gateway.routes;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DynamicRouteLoaderTest {

    private RouteDefinitionWriter writer;
    private NacosConfigManager nacosConfigManager;
    private ConfigService configService;
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setUp() {
        writer = mock(RouteDefinitionWriter.class);
        nacosConfigManager = mock(NacosConfigManager.class);
        configService = mock(ConfigService.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        when(writer.save(any())).thenReturn(Mono.empty());
        when(writer.delete(any())).thenReturn(Mono.empty());
        when(nacosConfigManager.getConfigService()).thenReturn(configService);
    }

    @Test
    void registerRouteListenerWithRetry_shouldNotThrowWhenNacosClientIsStillStarting() throws NacosException {
        when(configService.getConfigAndSignListener(anyString(), anyString(), anyInt(), any(Listener.class)))
                .thenThrow(new NacosException(-401, "Client not connected, current status:STARTING"));

        DynamicRouteLoader loader = new DynamicRouteLoader(writer, nacosConfigManager, applicationEventPublisher);

        assertDoesNotThrow(loader::registerRouteListenerWithRetry);
    }

    @Test
    void parseRouteDefinitions_shouldSupportWrappedRoutesObject() {
        String config = """
                {
                  "routes": [
                    {
                      "id": "user-service",
                      "uri": "lb://yu-mall-user-center-service",
                      "predicates": [
                        {
                          "name": "Path",
                          "args": {
                            "_genkey_0": "/users/**"
                          }
                        }
                      ]
                    }
                  ]
                }
                """;

        List<org.springframework.cloud.gateway.route.RouteDefinition> routes =
                DynamicRouteLoader.parseRouteDefinitions(config);

        assertEquals(1, routes.size());
        assertEquals("user-service", routes.getFirst().getId());
        assertEquals(URI.create("lb://yu-mall-user-center-service"), routes.getFirst().getUri());
        assertEquals("Path", routes.getFirst().getPredicates().getFirst().getName());
    }

    @Test
    void parseRouteDefinitions_shouldSupportLegacyStringPredicateFormat() {
        String config = """
                {
                  "routes": [
                    {
                      "id": "user-service",
                      "uri": "lb://yu-mall-user-center-service",
                      "predicates": ["Path=/users/**,/upload/**"],
                      "filters": ["StripPrefix=1"]
                    }
                  ]
                }
                """;

        List<org.springframework.cloud.gateway.route.RouteDefinition> routes =
                DynamicRouteLoader.parseRouteDefinitions(config);

        assertEquals(1, routes.size());
        assertEquals("Path", routes.getFirst().getPredicates().getFirst().getName());
        assertEquals("/users/**", routes.getFirst().getPredicates().getFirst().getArgs().get("_genkey_0"));
        assertEquals("/upload/**", routes.getFirst().getPredicates().getFirst().getArgs().get("_genkey_1"));
        assertEquals("StripPrefix", routes.getFirst().getFilters().getFirst().getName());
    }

    @Test
    void updateConfig_shouldLoadAndReplaceRoutesAndPublishRefreshEvent() {
        List<String> savedRouteIds = new ArrayList<>();
        List<String> deletedRouteIds = new ArrayList<>();
        when(writer.save(any())).thenAnswer(invocation -> {
            Mono<org.springframework.cloud.gateway.route.RouteDefinition> routeMono = invocation.getArgument(0);
            savedRouteIds.add(routeMono.block().getId());
            return Mono.empty();
        });
        when(writer.delete(any())).thenAnswer(invocation -> {
            Mono<String> routeIdMono = invocation.getArgument(0);
            deletedRouteIds.add(routeIdMono.block());
            return Mono.empty();
        });
        DynamicRouteLoader loader = new DynamicRouteLoader(writer, nacosConfigManager, applicationEventPublisher);

        loader.updateConfig("""
                {
                  "routes": [
                    {
                      "id": "item",
                      "uri": "lb://yu-mall-product-service",
                      "predicates": ["Path=/items/**,/shops/**"]
                    }
                  ]
                }
                """);

        assertEquals(List.of("item"), savedRouteIds);
        assertEquals(List.of(), deletedRouteIds);
        verify(applicationEventPublisher).publishEvent(any(RefreshRoutesEvent.class));

        loader.updateConfig("""
                {
                  "routes": [
                    {
                      "id": "search",
                      "uri": "lb://yu-mall-search-service",
                      "predicates": ["Path=/search/**"]
                    }
                  ]
                }
                """);

        assertEquals(List.of("item", "search"), savedRouteIds);
        assertEquals(List.of("item"), deletedRouteIds);
        verify(applicationEventPublisher, times(2)).publishEvent(any(RefreshRoutesEvent.class));
    }
}
