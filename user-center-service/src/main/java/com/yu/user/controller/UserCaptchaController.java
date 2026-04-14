package com.yu.user.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.yu.common.domain.AjaxResult;
import com.yu.common.exception.RateLimitException;
import com.yu.common.utils.WebUtils;
import com.yu.user.domain.dto.SliderCaptchaChallengeDTO;
import com.yu.user.domain.dto.SliderCaptchaVerifyDTO;
import com.yu.user.domain.vo.SliderCaptchaChallengeVO;
import com.yu.user.domain.vo.SliderCaptchaVerifyVO;
import com.yu.user.service.IUserCaptchaService;
import com.yu.user.support.AuthRiskPolicy;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Supplier;

@RestController
@RequestMapping("/users/captcha/slider")
@RequiredArgsConstructor
public class UserCaptchaController {

    private final IUserCaptchaService userCaptchaService;
    private final AuthRiskPolicy authRiskPolicy;

    @PostMapping("/challenge")
    @ApiOperation("创建滑块挑战")
    public AjaxResult<SliderCaptchaChallengeVO> createChallenge(@RequestBody @Valid SliderCaptchaChallengeDTO challengeDTO) {
        return withSentinel("captcha-challenge", () -> {
            authRiskPolicy.guardChallenge(WebUtils.getClientIp());
            return AjaxResult.success(userCaptchaService.createChallenge(challengeDTO.getScene()));
        });
    }

    @PostMapping("/verify")
    @ApiOperation("校验滑块挑战")
    public AjaxResult<SliderCaptchaVerifyVO> verify(@RequestBody @Valid SliderCaptchaVerifyDTO verifyDTO) {
        return withSentinel("captcha-verify", () -> {
            authRiskPolicy.guardVerify(WebUtils.getClientIp());
            return AjaxResult.success(userCaptchaService.verifyChallenge(verifyDTO));
        });
    }

    private <T> AjaxResult<T> withSentinel(String resource, Supplier<AjaxResult<T>> supplier) {
        Entry entry = null;
        try {
            entry = SphU.entry(resource);
            return supplier.get();
        } catch (BlockException e) {
            throw new RateLimitException("操作过于频繁，请稍后再试", e);
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}
