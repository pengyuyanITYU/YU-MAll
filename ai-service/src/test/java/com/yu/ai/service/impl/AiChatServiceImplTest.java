package com.yu.ai.service.impl;

import com.yu.ai.config.YuAiProperties;
import com.yu.ai.service.AiChatExecutor;
import com.yu.api.dto.AiChatRequestDTO;
import com.yu.api.vo.AiChatResponseVO;
import com.yu.common.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiChatServiceImplTest {

    private final YuAiProperties yuAiProperties = new YuAiProperties();
    private final AiChatExecutor aiChatExecutor = mock(AiChatExecutor.class);
    private AiChatServiceImpl aiChatService;

    @BeforeEach
    void setUp() {
        yuAiProperties.setDefaultModel("gpt-4o-mini");
        yuAiProperties.setDefaultTemperature(0.7);
        aiChatService = new AiChatServiceImpl(yuAiProperties, aiChatExecutor);
    }

    @Test
    void chat_shouldThrowBadRequestException_whenMessageBlank() {
        AiChatRequestDTO requestDTO = new AiChatRequestDTO();
        requestDTO.setMessage(" ");

        assertThrows(BadRequestException.class, () -> aiChatService.chat(requestDTO));
    }

    @Test
    void chat_shouldUseDefaultModel_whenRequestModelBlank() {
        AiChatRequestDTO requestDTO = new AiChatRequestDTO();
        requestDTO.setMessage("你好");
        requestDTO.setTemperature(0.3);
        when(aiChatExecutor.chat("你好", "gpt-4o-mini", 0.3)).thenReturn("收到");

        AiChatResponseVO responseVO = aiChatService.chat(requestDTO);

        assertEquals("收到", responseVO.getReply());
        assertEquals("gpt-4o-mini", responseVO.getModel());
        verify(aiChatExecutor).chat("你好", "gpt-4o-mini", 0.3);
    }

    @Test
    void chat_shouldUseRequestModel_whenRequestModelProvided() {
        AiChatRequestDTO requestDTO = new AiChatRequestDTO();
        requestDTO.setMessage("你好");
        requestDTO.setModel("gpt-4.1-mini");
        requestDTO.setTemperature(0.1);
        when(aiChatExecutor.chat("你好", "gpt-4.1-mini", 0.1)).thenReturn("已切换模型");

        AiChatResponseVO responseVO = aiChatService.chat(requestDTO);

        assertEquals("已切换模型", responseVO.getReply());
        assertEquals("gpt-4.1-mini", responseVO.getModel());
        verify(aiChatExecutor).chat("你好", "gpt-4.1-mini", 0.1);
    }
}