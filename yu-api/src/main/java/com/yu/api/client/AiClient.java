package com.yu.api.client;

import com.yu.api.dto.AiChatRequestDTO;
import com.yu.api.vo.AiChatResponseVO;
import com.yu.common.domain.AjaxResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "yu-mall-ai-service", path = "/ai")
public interface AiClient {

    @PostMapping("/chat")
    AjaxResult<AiChatResponseVO> chat(@RequestBody AiChatRequestDTO requestDTO);
}
