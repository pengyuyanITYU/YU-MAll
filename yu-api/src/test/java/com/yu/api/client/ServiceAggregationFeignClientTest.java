package com.yu.api.client;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceAggregationFeignClientTest {

    @Test
    void userDomainClients_shouldTargetUserCenterService() {
        assertFeignService(UserClient.class, "yu-mall-user-center-service");
        assertFeignService(AddressClient.class, "yu-mall-user-center-service");
        assertFeignService(MemberClient.class, "yu-mall-user-center-service");
    }

    @Test
    void productAndOrderClients_shouldTargetAggregatedServices() {
        assertFeignService(ItemClient.class, "yu-mall-product-service");
        assertFeignService(CartClient.class, "yu-mall-order-service");
        assertFeignService(OrderClient.class, "yu-mall-order-service");
    }

    @Test
    void aggregatedServiceClients_shouldDeclareUniqueContextIds() {
        assertUniqueContextIds(
                UserClient.class,
                AddressClient.class,
                MemberClient.class
        );
        assertUniqueContextIds(
                CartClient.class,
                OrderClient.class
        );
    }

    private static void assertFeignService(Class<?> clientType, String expectedServiceName) {
        FeignClient feignClient = clientType.getAnnotation(FeignClient.class);
        assertEquals(expectedServiceName, resolveServiceName(feignClient), clientType.getSimpleName());
    }

    private static void assertUniqueContextIds(Class<?>... clientTypes) {
        Set<String> contextIds = new HashSet<>();
        for (Class<?> clientType : clientTypes) {
            FeignClient feignClient = clientType.getAnnotation(FeignClient.class);
            assertFalse(feignClient.contextId().isBlank(), clientType.getSimpleName());
            assertTrue(contextIds.add(feignClient.contextId()), clientType.getSimpleName());
        }
    }

    private static String resolveServiceName(FeignClient feignClient) {
        if (!feignClient.name().isBlank()) {
            return feignClient.name();
        }
        return feignClient.value();
    }
}
