package com.yu.common.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AutoConfigurationImportsTest {

    @Test
    void shouldNotAutoImportRabbitMqConfig() throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
                "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports")) {
            assertNotNull(inputStream);
            String imports = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            assertFalse(imports.contains("com.yu.common.config.RabbitMQConfig"));
        }
    }
}
