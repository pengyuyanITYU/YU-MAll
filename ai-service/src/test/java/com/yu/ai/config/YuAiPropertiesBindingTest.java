package com.yu.ai.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class YuAiPropertiesBindingTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class);

    @Test
    void shouldFailWhenRequiredPropertiesMissing() {
        contextRunner.run(context ->
                assertThat(context.getStartupFailure()).isNotNull());
    }

    @Test
    void shouldBindWhenRequiredPropertiesProvided() {
        contextRunner
                .withPropertyValues(
                        "yu.ai.default-model=qwen-max",
                        "yu.ai.system-prompt=You are a test assistant.",
                        "yu.ai.default-temperature=0.2"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    YuAiProperties properties = context.getBean(YuAiProperties.class);
                    assertThat(properties.getDefaultModel()).isEqualTo("qwen-max");
                    assertThat(properties.getSystemPrompt()).isEqualTo("You are a test assistant.");
                    assertThat(properties.getDefaultTemperature()).isEqualTo(0.2D);
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(YuAiProperties.class)
    static class TestConfiguration {
    }
}
