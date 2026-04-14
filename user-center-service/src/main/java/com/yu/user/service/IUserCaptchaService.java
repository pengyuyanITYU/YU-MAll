package com.yu.user.service;

import com.yu.user.domain.dto.SliderCaptchaVerifyDTO;
import com.yu.user.domain.vo.SliderCaptchaChallengeVO;
import com.yu.user.domain.vo.SliderCaptchaVerifyVO;

public interface IUserCaptchaService {

    SliderCaptchaChallengeVO createChallenge(String scene);

    SliderCaptchaVerifyVO verifyChallenge(SliderCaptchaVerifyDTO verifyDTO);

    void consumeCaptchaTicket(String captchaTicket, String scene);
}
