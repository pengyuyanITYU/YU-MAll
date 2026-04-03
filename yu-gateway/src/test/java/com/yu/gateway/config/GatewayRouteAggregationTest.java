package com.yu.gateway.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewayRouteAggregationTest {

    @Test
    void applicationYaml_shouldRouteUserDomainPathsToUserCenterService() throws IOException {
        String yaml = readGatewayYaml();
        assertTrue(yaml.contains("uri: lb://yu-mall-user-center-service"));
    }

    @Test
    void applicationYaml_shouldRouteProductAndOrderDomainPathsToAggregatedServices() throws IOException {
        String yaml = readGatewayYaml();
        assertTrue(yaml.contains("uri: lb://yu-mall-product-service"));
        assertTrue(yaml.contains("uri: lb://yu-mall-order-service"));
    }

    @Test
    void applicationYaml_shouldRouteAiPathsToAiService() throws IOException {
        String yaml = readGatewayYaml();
        assertTrue(yaml.contains("uri: lb://yu-mall-ai-service"));
        assertTrue(yaml.contains("- Path=/ai/**"));
    }

    private static String readGatewayYaml() throws IOException {
        return Files.readString(Path.of("src", "main", "resources", "application.yaml"));
    }
}
