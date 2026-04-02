package com.yu.ai.service.impl;

import com.yu.ai.config.YuAiProperties;
import com.yu.ai.service.AiChatExecutor;
import com.yu.ai.service.IAiChatService;
import com.yu.api.dto.AiChatRequestDTO;
import com.yu.api.vo.AiChatResponseVO;
import com.yu.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements IAiChatService {

    private final YuAiProperties yuAiProperties;
    private final AiChatExecutor aiChatExecutor;

    @Override
    public AiChatResponseVO chat(AiChatRequestDTO requestDTO) {
        if (requestDTO == null || !StringUtils.hasText(requestDTO.getMessage())) {
            throw new BadRequestException("消息不能为空");
        }

        String model = StringUtils.hasText(requestDTO.getModel())
                ? requestDTO.getModel()
                : yuAiProperties.getDefaultModel();
        Double temperature = requestDTO.getTemperature() == null
                ? yuAiProperties.getDefaultTemperature()
                : requestDTO.getTemperature();
        String reply = aiChatExecutor.chat(requestDTO.getMessage(), model, temperature);

        AiChatResponseVO responseVO = new AiChatResponseVO();
        responseVO.setReply(reply);
        responseVO.setModel(model);
        return responseVO;
    }
}