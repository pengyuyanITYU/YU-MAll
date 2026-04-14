package com.yu.user.service.impl;

import com.yu.common.exception.BadRequestException;
import com.yu.user.domain.dto.SliderCaptchaVerifyDTO;
import com.yu.user.domain.vo.SliderCaptchaChallengeVO;
import com.yu.user.support.SliderCaptchaGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCaptchaServiceImplTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private SliderCaptchaGenerator sliderCaptchaGenerator;

    private UserCaptchaServiceImpl userCaptchaService;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        userCaptchaService = new UserCaptchaServiceImpl(
                stringRedisTemplate,
                sliderCaptchaGenerator,
                new ObjectMapper()
        );
    }

    @Test
    void shouldRejectExpiredChallengeWhenVerify() {
        SliderCaptchaVerifyDTO dto = new SliderCaptchaVerifyDTO();
        dto.setCaptchaId("captcha-1");
        dto.setOffsetX(48);

        assertThrows(BadRequestException.class, () -> userCaptchaService.verifyChallenge(dto));
    }

    @Test
    void shouldRejectTrackWithBackwardMovement() throws Exception {
        SliderCaptchaVerifyDTO dto = new SliderCaptchaVerifyDTO();
        dto.setCaptchaId("captcha-2");
        dto.setOffsetX(48);
        dto.setDragTrack(List.of(
                new SliderCaptchaVerifyDTO.TrackPoint(0, 0, 1L),
                new SliderCaptchaVerifyDTO.TrackPoint(12, 1, 2L),
                new SliderCaptchaVerifyDTO.TrackPoint(10, 1, 3L),
                new SliderCaptchaVerifyDTO.TrackPoint(18, 1, 4L),
                new SliderCaptchaVerifyDTO.TrackPoint(25, 1, 5L),
                new SliderCaptchaVerifyDTO.TrackPoint(31, 1, 6L),
                new SliderCaptchaVerifyDTO.TrackPoint(39, 1, 7L),
                new SliderCaptchaVerifyDTO.TrackPoint(48, 1, 8L)
        ));

        String challengeJson = OBJECT_MAPPER.writeValueAsString(
                new UserCaptchaServiceImpl.CaptchaChallengeCache(
                        "captcha-2",
                        "REGISTER",
                        48,
                        64,
                        System.currentTimeMillis()
                )
        );
        when(valueOperations.get("auth:captcha:challenge:captcha-2")).thenReturn(challengeJson);

        assertThrows(BadRequestException.class, () -> userCaptchaService.verifyChallenge(dto));
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void shouldDeleteTicketAfterSuccessfulConsumption() throws Exception {
        String ticketJson = OBJECT_MAPPER.writeValueAsString(
                new UserCaptchaServiceImpl.CaptchaTicketCache(
                        "ticket-1",
                        "REGISTER",
                        System.currentTimeMillis()
                )
        );
        when(valueOperations.get("auth:captcha:ticket:ticket-1")).thenReturn(ticketJson);

        assertDoesNotThrow(() -> userCaptchaService.consumeCaptchaTicket("ticket-1", "REGISTER"));
        verify(stringRedisTemplate).delete("auth:captcha:ticket:ticket-1");
    }

    @Test
    void shouldReturnTrackMetadataWhenCreateChallenge() {
        when(sliderCaptchaGenerator.generate(anyString())).thenReturn(
                new SliderCaptchaGenerator.GeneratedCaptcha(null, null, 112, 64, 320, 56)
        );

        SliderCaptchaChallengeVO challenge = userCaptchaService.createChallenge("REGISTER");

        assertEquals("track", challenge.getRenderMode());
        assertEquals(320, challenge.getTrackWidth());
        assertEquals(56, challenge.getHandleWidth());
        assertEquals(112, challenge.getTargetLeft());
        assertEquals(64, challenge.getTargetWidth());
        assertNotNull(challenge.getCaptchaId());
        verify(valueOperations).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void shouldRejectTrackThatFinishesTooFast() throws Exception {
        SliderCaptchaVerifyDTO dto = new SliderCaptchaVerifyDTO();
        dto.setCaptchaId("captcha-3");
        dto.setOffsetX(124);
        dto.setDragTrack(List.of(
                new SliderCaptchaVerifyDTO.TrackPoint(0, 0, 0L),
                new SliderCaptchaVerifyDTO.TrackPoint(16, 0, 15L),
                new SliderCaptchaVerifyDTO.TrackPoint(32, 0, 30L),
                new SliderCaptchaVerifyDTO.TrackPoint(48, 0, 45L),
                new SliderCaptchaVerifyDTO.TrackPoint(64, 0, 60L),
                new SliderCaptchaVerifyDTO.TrackPoint(80, 0, 75L),
                new SliderCaptchaVerifyDTO.TrackPoint(96, 0, 90L),
                new SliderCaptchaVerifyDTO.TrackPoint(124, 0, 105L)
        ));

        String challengeJson = OBJECT_MAPPER.writeValueAsString(
                new UserCaptchaServiceImpl.CaptchaChallengeCache(
                        "captcha-3",
                        "REGISTER",
                        112,
                        64,
                        System.currentTimeMillis()
                )
        );
        when(valueOperations.get("auth:captcha:challenge:captcha-3")).thenReturn(challengeJson);

        assertThrows(BadRequestException.class, () -> userCaptchaService.verifyChallenge(dto));
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }
}
