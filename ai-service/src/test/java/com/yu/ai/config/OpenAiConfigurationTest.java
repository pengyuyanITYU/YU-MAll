package com.yu.ai.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenAiConfigurationTest {

    @Test
    void applicationYaml_shouldUseDashScopeCompatibleBaseUrlWithoutVersionSuffix() throws IOException {
        String yaml = Files.readString(Path.of("src", "main", "resources", "application.yaml"));
        assertTrue(yaml.contains("base-url: https://dashscope.aliyuncs.com/compatible-mode"));
        assertFalse(yaml.contains("base-url: https://dashscope.aliyuncs.com/compatible-mode/v1"));
    }
}