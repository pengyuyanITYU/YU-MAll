package com.yu.user.support;

import cn.hutool.core.util.StrUtil;
import com.yu.common.exception.CaptchaRequiredException;
import com.yu.common.exception.RateLimitException;
import com.yu.user.service.IUserCaptchaService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class AuthRiskPolicy {

    private static final Duration LOGIN_FAILURE_TTL = Duration.ofMinutes(10);
    private static final long LOGIN_CAPTCHA_THRESHOLD = 5L;
    private static final long LOW_TOKEN_THRESHOLD = 2L;

    private final StringRedisTemplate stringRedisTemplate;
    private final AuthTokenBucketLimiter tokenBucketLimiter;
    private final IUserCaptchaService userCaptchaService;

    public AuthRiskPolicy(StringRedisTemplate stringRedisTemplate,
                          AuthTokenBucketLimiter tokenBucketLimiter,
                          IUserCaptchaService userCaptchaService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.tokenBucketLimiter = tokenBucketLimiter;
        this.userCaptchaService = userCaptchaService;
    }

    public void guardChallenge(String clientIp) {
        assertAllowed(tokenBucketLimiter.tryConsume("challenge:global", "global"));
        assertAllowed(tokenBucketLimiter.tryConsume("register:ip", normalize(clientIp)));
    }

    public void guardVerify(String clientIp) {
        assertAllowed(tokenBucketLimiter.tryConsume("verify:global", "global"));
        assertAllowed(tokenBucketLimiter.tryConsume("register:ip", normalize(clientIp)));
    }

    public void guardRegister(String username, String phone, String clientIp, String captchaTicket) {
        assertAllowed(tokenBucketLimiter.tryConsume("register:global", "global"));
        assertAllowed(tokenBucketLimiter.tryConsume("register:username", normalize(username)));
        assertAllowed(tokenBucketLimiter.tryConsume("register:phone", normalize(phone)));
        assertAllowed(tokenBucketLimiter.tryConsume("register:ip", normalize(clientIp)));
        if (StrUtil.isBlank(captchaTicket)) {
            throw new CaptchaRequiredException("注册前请先完成滑块验证");
        }
        userCaptchaService.consumeCaptchaTicket(captchaTicket, "REGISTER");
    }

    public void guardLogin(String username, String clientIp, String captchaTicket) {
        AuthTokenBucketLimiter.TokenBucketResult global = tokenBucketLimiter.tryConsume("login:global", "global");
        AuthTokenBucketLimiter.TokenBucketResult user = tokenBucketLimiter.tryConsume("login:username", normalize(username));
        AuthTokenBucketLimiter.TokenBucketResult ip = tokenBucketLimiter.tryConsume("login:ip", normalize(clientIp));
        assertAllowed(global);
        assertAllowed(user);
        assertAllowed(ip);
        boolean requireCaptcha = getLoginFailureCount(username) >= LOGIN_CAPTCHA_THRESHOLD
                || user.remainingTokens() <= LOW_TOKEN_THRESHOLD
                || ip.remainingTokens() <= LOW_TOKEN_THRESHOLD;
        if (!requireCaptcha) {
            return;
        }
        if (StrUtil.isBlank(captchaTicket)) {
            throw new CaptchaRequiredException("当前登录需要先完成滑块验证");
        }
        userCaptchaService.consumeCaptchaTicket(captchaTicket, "LOGIN");
    }

    public void recordLoginFailure(String username) {
        String key = loginFailureKey(username);
        stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.expire(key, LOGIN_FAILURE_TTL);
    }

    public void clearLoginFailures(String username) {
        stringRedisTemplate.delete(loginFailureKey(username));
    }

    private long getLoginFailureCount(String username) {
        String cacheValue = stringRedisTemplate.opsForValue().get(loginFailureKey(username));
        if (StrUtil.isBlank(cacheValue)) {
            return 0L;
        }
        return Long.parseLong(cacheValue);
    }

    private void assertAllowed(AuthTokenBucketLimiter.TokenBucketResult result) {
        if (!result.allowed()) {
            throw new RateLimitException("操作过于频繁，请稍后再试");
        }
    }

    private String loginFailureKey(String username) {
        return "auth:login:fail:" + hashKey(normalize(username));
    }

    private String normalize(String rawValue) {
        return StrUtil.blankToDefault(StrUtil.trim(rawValue), "unknown");
    }

    private String hashKey(String rawValue) {
        return DigestUtils.md5DigestAsHex(rawValue.getBytes(StandardCharsets.UTF_8));
    }
}
