package com.yu.admin;

import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cache.annotation.EnableCaching;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AdminApplicationConfigurationTest {

    @Test
    void shouldDeclareRuntimeAnnotationsRequiredByAdminService() {
        MapperScan mapperScan = AdminApplication.class.getAnnotation(MapperScan.class);
        EnableFileStorage enableFileStorage = AdminApplication.class.getAnnotation(EnableFileStorage.class);
        EnableCaching enableCaching = AdminApplication.class.getAnnotation(EnableCaching.class);

        assertNotNull(mapperScan);
        assertArrayEquals(new String[]{"com.yu.admin.mapper"},
                mapperScan.value().length > 0 ? mapperScan.value() : mapperScan.basePackages());
        assertNotNull(enableFileStorage);
        assertNotNull(enableCaching);
    }
}
