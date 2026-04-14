package com.yu.user.support;

import com.yu.common.exception.CaptchaRequiredException;
import com.yu.user.service.IUserCaptchaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthRiskPolicyTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private AuthTokenBucketLimiter tokenBucketLimiter;
    @Mock
    private IUserCaptchaService userCaptchaService;

    private AuthRiskPolicy authRiskPolicy;

    @BeforeEach
    void setUp() {
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        authRiskPolicy = new AuthRiskPolicy(stringRedisTemplate, tokenBucketLimiter, userCaptchaService);
    }

    @Test
    void shouldRequireCaptchaForLoginWhenFailureThresholdReached() {
        when(valueOperations.get(anyString())).thenReturn("5");
        when(tokenBucketLimiter.tryConsume(anyString(), anyString()))
                .thenReturn(new AuthTokenBucketLimiter.TokenBucketResult(true, 8L));

        assertThrows(CaptchaRequiredException.class,
                () -> authRiskPolicy.guardLogin("demo", "127.0.0.1", null));

        verify(userCaptchaService, never()).consumeCaptchaTicket(anyString(), anyString());
    }

    @Test
    void shouldConsumeCaptchaTicketForRegister() {
        when(tokenBucketLimiter.tryConsume(anyString(), anyString()))
                .thenReturn(new AuthTokenBucketLimiter.TokenBucketResult(true, 12L));

        authRiskPolicy.guardRegister("demo", "13800138000", "127.0.0.1", "ticket-1");

        verify(userCaptchaService).consumeCaptchaTicket("ticket-1", "REGISTER");
    }
}
