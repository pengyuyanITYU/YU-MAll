package com.yu.ai.controller;

import com.yu.ai.service.IAiChatService;
import com.yu.api.dto.AiChatRequestDTO;
import com.yu.api.vo.AiChatResponseVO;
import com.yu.common.domain.AjaxResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final IAiChatService aiChatService;

    @PostMapping("/chat")
    public AjaxResult<AiChatResponseVO> chat(@Valid @RequestBody AiChatRequestDTO requestDTO) {
        return AjaxResult.success(aiChatService.chat(requestDTO));
    }
}
