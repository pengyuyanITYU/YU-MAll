package com.yu.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedisConfigTest {

    @Test
    void shouldIgnoreCacheErrorsWhenRedisIsUnavailable() {
        CacheErrorHandler errorHandler = new RedisConfig().errorHandler();
        Cache cache = mock(Cache.class);
        when(cache.getName()).thenReturn("item");

        assertDoesNotThrow(() -> errorHandler.handleCacheGetError(new RuntimeException("boom"), cache, "id:10"));
        assertDoesNotThrow(() -> errorHandler.handleCachePutError(new RuntimeException("boom"), cache, "id:10", "value"));
        assertDoesNotThrow(() -> errorHandler.handleCacheEvictError(new RuntimeException("boom"), cache, "id:10"));
        assertDoesNotThrow(() -> errorHandler.handleCacheClearError(new RuntimeException("boom"), cache));
    }
}
