package com.yu.product;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProductApplicationMapperScanTest {

    @Test
    void shouldDeclareMapperScanForAllMergedDomains() {
        MapperScan mapperScan = ProductApplication.class.getAnnotation(MapperScan.class);

        assertNotNull(mapperScan);
        assertArrayEquals(new String[]{
                "com.yu.item.mapper",
                "com.yu.comment.mapper",
                "com.yu.collect.mapper"
        }, mapperScan.value().length > 0 ? mapperScan.value() : mapperScan.basePackages());
    }
}
