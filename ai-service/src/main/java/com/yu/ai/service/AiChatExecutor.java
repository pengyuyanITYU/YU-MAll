package com.yu.ai.service;

public interface AiChatExecutor {

    String chat(String message, String model, Double temperature);
}