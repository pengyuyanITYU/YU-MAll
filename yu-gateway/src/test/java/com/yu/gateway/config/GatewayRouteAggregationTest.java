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
    void applicationYaml_shouldRouteShopPathsToProductService() throws IOException {
        String yaml = readGatewayYaml();
        assertTrue(yaml.contains("/shops/**"));
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
        assertTrue(!yaml.contains("/admin/items/**"));
        assertTrue(!yaml.contains("/admin/categories/**"));
        assertTrue(!yaml.contains("/admin/shops/**"));
        assertTrue(!yaml.contains("/admin/comments/**"));
        assertTrue(!yaml.contains("/admin/users/**"));
        assertTrue(!yaml.contains("/admin/orders/**"));
    }

    private static String readGatewayYaml() throws IOException {
        return Files.readString(Path.of("src", "main", "resources", "application.yaml"));
    }
}
