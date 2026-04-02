package com.yu.order;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderApplicationMapperScanTest {

    @Test
    void shouldDeclareMapperScanForAllMergedDomains() {
        MapperScan mapperScan = OrderApplication.class.getAnnotation(MapperScan.class);

        assertNotNull(mapperScan);
        assertArrayEquals(new String[]{
                "com.yu.order.mapper",
                "com.yu.cart.mapper"
        }, mapperScan.value().length > 0 ? mapperScan.value() : mapperScan.basePackages());
    }
}
