package com.yu.gateway.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewayRouteAggregationTest {

    @Test
    void applicationYaml_shouldUseWebfluxGatewayRouteNamespace() throws IOException {
        String yaml = readGatewayYaml();
        assertTrue(yaml.contains("webflux:"));
        assertTrue(yaml.contains("routes:"));
    }

    @Test
    void applicationYaml_shouldRouteUserDomainPathsToUserCenterService() throws IOException {
        String yaml = readGatewayYaml();
        assertTrue(yaml.contains("uri: lb://yu-mall-user-center-service"));
        assertTrue(yaml.contains("- Path=/users/**,/upload/**"));
    }

    @Test
    void applicationYaml_shouldRouteProductAndOrderDomainPathsToAggregatedServices() throws IOException {
        String yaml = readGatewayYaml();
        assertTrue(yaml.contains("uri: lb://yu-mall-product-service"));
        assertTrue(yaml.contains("uri: lb://yu-mall-order-service"));
        assertTrue(yaml.contains("- Path=/items/**,/shops/**"));
        assertFalse(yaml.contains("- Path=/items/**,/shops/**,/search/**"));
    }

    @Test
    void applicationYaml_shouldRouteSearchPathsOnlyToSearchService() throws IOException {
        String yaml = readGatewayYaml();
        assertTrue(yaml.contains("- id: search"));
        assertTrue(yaml.contains("uri: lb://yu-mall-search-service"));
        assertTrue(yaml.contains("- Path=/search/**"));
    }

    @Test
    void applicationYaml_shouldRouteAiPathsToAiService() throws IOException {
        String yaml = readGatewayYaml();
        assertTrue(yaml.contains("uri: lb://yu-mall-ai-service"));
        assertTrue(yaml.contains("- Path=/ai/**"));
    }

    @Test
    void applicationYaml_shouldRouteAdminPathsToAdminServiceOnly() throws IOException {
        String yaml = readGatewayYaml();
        assertTrue(yaml.contains("uri: lb://yu-mall-admin-service"));
        assertTrue(yaml.contains("- Path=/admin/**,/admins/**"));
        assertFalse(yaml.contains("/admin/items/**"));
        assertFalse(yaml.contains("/admin/categories/**"));
        assertFalse(yaml.contains("/admin/shops/**"));
        assertFalse(yaml.contains("/admin/comments/**"));
        assertFalse(yaml.contains("/admin/users/**"));
        assertFalse(yaml.contains("/admin/orders/**"));
    }

    private static String readGatewayYaml() throws IOException {
        return Files.readString(Path.of("src", "main", "resources", "application.yaml"));
    }
}
