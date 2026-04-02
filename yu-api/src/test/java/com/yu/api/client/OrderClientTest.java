package com.yu.api.client;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class OrderClientTest {

    @Test
    void updateOrderStatus_shouldUsePathWithoutIdPlaceholder() throws NoSuchMethodException {
        Method method = OrderClient.class.getMethod("updateOrderStatus", com.yu.api.dto.UpdateOrderStatusDTO.class);
        PutMapping putMapping = method.getAnnotation(PutMapping.class);
        assertArrayEquals(new String[]{"/updateStatus"}, putMapping.value());
    }
}
