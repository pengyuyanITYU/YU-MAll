package com.yu.ai.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ConfigurationPropertiesAutoConfiguration.class,
                    ValidationAutoConfiguration.class
            ))
            .withUserConfiguration(TestConfiguration.class)
            .withPropertyValues(
                    "spring.ai.openai.base-url=https://dashscope.aliyuncs.com/compatible-mode",
                    "spring.ai.openai.api-key=test-api-key",
                    "yu.ai.default-model=qwen-plus",
                    "yu.ai.system-prompt=You are a mall AI assistant.",
                    "yu.ai.default-temperature=0.7"
            );

    @Test
    void applicationConfiguration_shouldBindDashScopeBaseUrlAndYuAiProperties() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            Environment environment = context.getEnvironment();
            YuAiProperties properties = context.getBean(YuAiProperties.class);
            String baseUrl = environment.getProperty("spring.ai.openai.base-url");

            assertThat(baseUrl).isEqualTo("https://dashscope.aliyuncs.com/compatible-mode");
            assertThat(baseUrl).doesNotEndWith("/v1");
            assertThat(properties.getDefaultModel()).isEqualTo("qwen-plus");
            assertThat(properties.getSystemPrompt()).isEqualTo("You are a mall AI assistant.");
            assertThat(properties.getDefaultTemperature()).isEqualTo(0.7D);
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(YuAiProperties.class)
    static class TestConfiguration {
    }
}
