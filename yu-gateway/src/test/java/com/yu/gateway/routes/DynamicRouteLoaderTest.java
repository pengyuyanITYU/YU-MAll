package com.yu.gateway.routes;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DynamicRouteLoaderTest {

    @Test
    void registerRouteListenerWithRetry_shouldNotThrowWhenNacosClientIsStillStarting() throws NacosException {
        RouteDefinitionWriter writer = new NoopRouteDefinitionWriter();
        NacosConfigManager nacosConfigManager = mock(NacosConfigManager.class);
        ConfigService configService = mock(ConfigService.class);
        when(nacosConfigManager.getConfigService()).thenReturn(configService);
        when(configService.getConfigAndSignListener(anyString(), anyString(), anyInt(), any(Listener.class)))
                .thenThrow(new NacosException(-401, "Client not connected, current status:STARTING"));

        DynamicRouteLoader loader = new DynamicRouteLoader(writer, nacosConfigManager);

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

    private static final class NoopRouteDefinitionWriter implements RouteDefinitionWriter {
        @Override
        public Mono<Void> save(Mono<org.springframework.cloud.gateway.route.RouteDefinition> route) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(Mono<String> routeId) {
            return Mono.empty();
        }
    }
}
