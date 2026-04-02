package com.yu.usercenter;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserCenterApplicationMapperScanTest {

    @Test
    void shouldDeclareMapperScanForAllMergedDomains() {
        MapperScan mapperScan = UserCenterApplication.class.getAnnotation(MapperScan.class);

        assertNotNull(mapperScan);
        assertArrayEquals(new String[]{
                "com.yu.user.mapper",
                "com.yu.address.mapper",
                "com.yu.member.mapper"
        }, mapperScan.value().length > 0 ? mapperScan.value() : mapperScan.basePackages());
    }
}
