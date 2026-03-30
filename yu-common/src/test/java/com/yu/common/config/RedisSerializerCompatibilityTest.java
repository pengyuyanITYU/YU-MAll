package com.yu.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class RedisSerializerCompatibilityTest {

    @Test
    void shouldSerializeLocalDateTime() {
        RedisSerializer<Object> serializer = RedisConfig.cacheValueSerializer();
        Payload payload = new Payload();
        payload.setName("category");
        payload.setCreateTime(LocalDateTime.now());
        assertDoesNotThrow(() -> serializer.serialize(payload));
    }

    private static class Payload {
        private String name;
        private LocalDateTime createTime;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }
    }
}
