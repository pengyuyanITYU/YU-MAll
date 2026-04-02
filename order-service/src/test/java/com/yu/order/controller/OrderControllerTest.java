package com.yu.order.controller;

import com.yu.order.domain.dto.UpdateOrderStatusDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class OrderControllerTest {

    @Test
    void updateOrderStatus_shouldSupportTrailingSlashForBackwardCompatibility() throws NoSuchMethodException {
        Method method = OrderController.class.getMethod("updateOrderStatus", UpdateOrderStatusDTO.class);
        PutMapping putMapping = method.getAnnotation(PutMapping.class);
        assertArrayEquals(new String[]{"/updateStatus", "/updateStatus/"}, putMapping.value());
    }
}
