package com.yu.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "yu.ai")
public class YuAiProperties {

    /**
     * 默认模型
     */
    private String defaultModel = "qwen-plus";

    /**
     * 系统提示词
     */
    private String systemPrompt = "You are a helpful assistant.";

    /**
     * 默认温度
     */
    private Double defaultTemperature = 0.7D;
}