package com.yu.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yu.common.exception.BadRequestException;
import com.yu.user.domain.dto.SliderCaptchaVerifyDTO;
import com.yu.user.domain.vo.SliderCaptchaChallengeVO;
import com.yu.user.domain.vo.SliderCaptchaVerifyVO;
import com.yu.user.service.IUserCaptchaService;
import com.yu.user.support.SliderCaptchaGenerator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UserCaptchaServiceImpl implements IUserCaptchaService {

    private static final Duration CHALLENGE_TTL = Duration.ofSeconds(120);
    private static final Duration TICKET_TTL = Duration.ofSeconds(180);
    private static final Set<String> SUPPORTED_SCENES = Set.of("LOGIN", "REGISTER");
    private static final int POSITION_TOLERANCE = 6;
    private static final long MIN_TRACK_DURATION_MS = 120L;

    private final StringRedisTemplate stringRedisTemplate;
    private final SliderCaptchaGenerator sliderCaptchaGenerator;
    private final ObjectMapper objectMapper;

    public UserCaptchaServiceImpl(StringRedisTemplate stringRedisTemplate,
                                  SliderCaptchaGenerator sliderCaptchaGenerator,
                                  ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.sliderCaptchaGenerator = sliderCaptchaGenerator;
        this.objectMapper = objectMapper;
    }

    @Override
    public SliderCaptchaChallengeVO createChallenge(String scene) {
        String normalizedScene = normalizeScene(scene);
        String captchaId = UUID.randomUUID().toString();
        SliderCaptchaGenerator.GeneratedCaptcha generatedCaptcha = sliderCaptchaGenerator.generate(captchaId);
        CaptchaChallengeCache cache = new CaptchaChallengeCache(
                captchaId,
                normalizedScene,
                generatedCaptcha.targetLeft(),
                generatedCaptcha.targetWidth(),
                System.currentTimeMillis()
        );
        stringRedisTemplate.opsForValue().set(challengeKey(captchaId), writeValue(cache), CHALLENGE_TTL);

        SliderCaptchaChallengeVO vo = new SliderCaptchaChallengeVO();
        vo.setCaptchaId(captchaId);
        vo.setRenderMode("track");
        vo.setTrackWidth(generatedCaptcha.trackWidth());
        vo.setHandleWidth(generatedCaptcha.handleWidth());
        vo.setTargetLeft(generatedCaptcha.targetLeft());
        vo.setTargetWidth(generatedCaptcha.targetWidth());
        vo.setBackgroundImage(generatedCaptcha.backgroundImage());
        vo.setSliderImage(generatedCaptcha.sliderImage());
        vo.setExpiresAt(System.currentTimeMillis() + CHALLENGE_TTL.toMillis());
        return vo;
    }

    @Override
    public SliderCaptchaVerifyVO verifyChallenge(SliderCaptchaVerifyDTO verifyDTO) {
        String captchaId = StrUtil.trimToEmpty(verifyDTO.getCaptchaId());
        String challengeJson = stringRedisTemplate.opsForValue().get(challengeKey(captchaId));
        if (StrUtil.isBlank(challengeJson)) {
            throw new BadRequestException("安全验证已过期，请刷新后重试");
        }

        CaptchaChallengeCache cache = readValue(challengeJson, CaptchaChallengeCache.class);
        validateTrack(verifyDTO.getDragTrack(), verifyDTO.getOffsetX());
        if (!isWithinTarget(cache, verifyDTO.getOffsetX())) {
            throw new BadRequestException("未进入目标区域，请再试一次");
        }

        String ticket = UUID.randomUUID().toString();
        CaptchaTicketCache ticketCache = new CaptchaTicketCache(ticket, cache.scene(), System.currentTimeMillis());
        stringRedisTemplate.opsForValue().set(ticketKey(ticket), writeValue(ticketCache), TICKET_TTL);
        stringRedisTemplate.delete(challengeKey(cache.captchaId()));

        SliderCaptchaVerifyVO vo = new SliderCaptchaVerifyVO();
        vo.setCaptchaTicket(ticket);
        vo.setExpiresAt(System.currentTimeMillis() + TICKET_TTL.toMillis());
        return vo;
    }

    @Override
    public void consumeCaptchaTicket(String captchaTicket, String scene) {
        if (StrUtil.isBlank(captchaTicket)) {
            throw new BadRequestException("验证码票据不能为空");
        }

        String ticketJson = stringRedisTemplate.opsForValue().get(ticketKey(captchaTicket));
        if (StrUtil.isBlank(ticketJson)) {
            throw new BadRequestException("验证码票据已失效，请重新验证");
        }

        CaptchaTicketCache cache = readValue(ticketJson, CaptchaTicketCache.class);
        if (!normalizeScene(scene).equals(cache.scene())) {
            throw new BadRequestException("验证码票据场景不匹配");
        }

        stringRedisTemplate.delete(ticketKey(captchaTicket));
    }

    private boolean isWithinTarget(CaptchaChallengeCache cache, Integer offsetX) {
        if (offsetX == null) {
            return false;
        }
        int minLeft = cache.targetLeft() - POSITION_TOLERANCE;
        int maxLeft = cache.targetLeft() + cache.targetWidth() + POSITION_TOLERANCE;
        return offsetX >= minLeft && offsetX <= maxLeft;
    }

    private void validateTrack(List<SliderCaptchaVerifyDTO.TrackPoint> dragTrack, Integer offsetX) {
        if (dragTrack == null || dragTrack.size() < 8) {
            throw new BadRequestException("拖动轨迹异常，请重试");
        }

        SliderCaptchaVerifyDTO.TrackPoint previous = null;
        for (SliderCaptchaVerifyDTO.TrackPoint point : dragTrack) {
            if (point.getX() == null || point.getY() == null || point.getT() == null) {
                throw new BadRequestException("拖动轨迹异常，请重试");
            }
            if (previous != null && (point.getX() < previous.getX() || point.getT() < previous.getT())) {
                throw new BadRequestException("拖动轨迹异常，请重试");
            }
            previous = point;
        }

        if (previous == null || previous.getT() < MIN_TRACK_DURATION_MS) {
            throw new BadRequestException("验证操作过快，请按提示拖动滑块");
        }
        if (offsetX == null || Math.abs(previous.getX() - offsetX) > POSITION_TOLERANCE) {
            throw new BadRequestException("拖动轨迹异常，请重试");
        }
    }

    private String normalizeScene(String scene) {
        String normalizedScene = StrUtil.trimToEmpty(scene).toUpperCase(Locale.ROOT);
        if (!SUPPORTED_SCENES.contains(normalizedScene)) {
            throw new BadRequestException("不支持的滑块场景");
        }
        return normalizedScene;
    }

    private String challengeKey(String captchaId) {
        return "auth:captcha:challenge:" + captchaId;
    }

    private String ticketKey(String captchaTicket) {
        return "auth:captcha:ticket:" + captchaTicket;
    }

    private String writeValue(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize captcha cache", e);
        }
    }

    private <T> T readValue(String value, Class<T> type) {
        try {
            return objectMapper.readValue(value, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize captcha cache", e);
        }
    }

    public record CaptchaChallengeCache(String captchaId, String scene, int targetLeft, int targetWidth, long issuedAt) {
    }

    public record CaptchaTicketCache(String captchaTicket, String scene, long issuedAt) {
    }
}
