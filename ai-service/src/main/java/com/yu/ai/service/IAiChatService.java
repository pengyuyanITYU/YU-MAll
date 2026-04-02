package com.yu.ai.service;

import com.yu.api.dto.AiChatRequestDTO;
import com.yu.api.vo.AiChatResponseVO;

public interface IAiChatService {

    AiChatResponseVO chat(AiChatRequestDTO requestDTO);
}
