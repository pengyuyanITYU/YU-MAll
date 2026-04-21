package com.yu.ai.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "yu.ai")
public class YuAiProperties {

    @NotBlank
    private String defaultModel;

    @NotBlank
    private String systemPrompt;

    private Double defaultTemperature = 0.7D;
}
