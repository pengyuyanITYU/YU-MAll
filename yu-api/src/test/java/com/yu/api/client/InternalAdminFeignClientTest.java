package com.yu.api.client;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternalAdminFeignClientTest {

    @Test
    void internalAdminClientsShouldTargetOwningServices() {
        assertFeignService(InternalAdminItemClient.class, "yu-mall-product-service");
        assertFeignService(InternalAdminCategoryClient.class, "yu-mall-product-service");
        assertFeignService(InternalAdminBrandClient.class, "yu-mall-product-service");
        assertFeignService(InternalAdminShopClient.class, "yu-mall-product-service");
        assertFeignService(InternalAdminCommentClient.class, "yu-mall-product-service");
        assertFeignService(InternalAdminUserClient.class, "yu-mall-user-center-service");
        assertFeignService(InternalAdminOrderClient.class, "yu-mall-order-service");
    }

    @Test
    void internalAdminClientsShouldDeclareUniqueContextIds() {
        assertUniqueContextIds(
                InternalAdminItemClient.class,
                InternalAdminCategoryClient.class,
                InternalAdminBrandClient.class,
                InternalAdminShopClient.class,
                InternalAdminCommentClient.class,
                InternalAdminUserClient.class,
                InternalAdminOrderClient.class
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
