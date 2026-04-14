package com.yu.api.client;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicAdminFeignClientExposureTest {

    @Test
    void shouldNotDeclareFeignClientsAgainstPublicAdminPaths() throws ClassNotFoundException {
        List<String> adminClients = new ArrayList<>();
        for (String clientClassName : knownClientClassNames()) {
            Class<?> clientClass = tryLoad(clientClassName);
            if (clientClass == null) {
                continue;
            }
            FeignClient feignClient = clientClass.getAnnotation(FeignClient.class);
            if (feignClient == null) {
                continue;
            }
            String path = feignClient.path();
            if (path != null && path.startsWith("/admin")) {
                adminClients.add(clientClass.getName() + " -> " + path);
            }
        }

        assertTrue(adminClients.isEmpty(), () -> "公开 /admin Feign 契约未清理: " + adminClients);
    }

    private static List<String> knownClientClassNames() {
        return Arrays.asList(
                "com.yu.api.client.UserClient",
                "com.yu.api.client.AddressClient",
                "com.yu.api.client.MemberClient",
                "com.yu.api.client.ItemClient",
                "com.yu.api.client.CartClient",
                "com.yu.api.client.OrderClient",
                "com.yu.api.client.AdminUserClient",
                "com.yu.api.client.InternalAdminUserClient",
                "com.yu.api.client.InternalAdminOrderClient",
                "com.yu.api.client.InternalAdminItemClient",
                "com.yu.api.client.InternalAdminCategoryClient",
                "com.yu.api.client.InternalAdminBrandClient",
                "com.yu.api.client.InternalAdminShopClient",
                "com.yu.api.client.InternalAdminCommentClient"
        );
    }

    private static Class<?> tryLoad(String className) throws ClassNotFoundException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            if (className.endsWith("AdminUserClient")) {
                return null;
            }
            throw ex;
        }
    }
}
