package com.yu.gateway.routes;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
