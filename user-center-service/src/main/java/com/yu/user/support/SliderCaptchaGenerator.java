package com.yu.user.support;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class SliderCaptchaGenerator {

    public static final int TRACK_WIDTH = 320;
    public static final int HANDLE_WIDTH = 56;
    public static final int TARGET_WIDTH = 64;
    private static final int TARGET_MIN_LEFT = 96;
    private static final int TARGET_MAX_LEFT = TRACK_WIDTH - HANDLE_WIDTH - TARGET_WIDTH;

    public GeneratedCaptcha generate(String seed) {
        int targetLeft = ThreadLocalRandom.current().nextInt(TARGET_MIN_LEFT, TARGET_MAX_LEFT + 1);
        return new GeneratedCaptcha(null, null, targetLeft, TARGET_WIDTH, TRACK_WIDTH, HANDLE_WIDTH);
    }

    public record GeneratedCaptcha(
            String backgroundImage,
            String sliderImage,
            int targetLeft,
            int targetWidth,
            int trackWidth,
            int handleWidth
    ) {
    }
}
