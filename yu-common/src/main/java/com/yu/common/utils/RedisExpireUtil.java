package com.yu.common.utils;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Redis 过期时间生成工具类
 * 用于生成带有随机抖动的过期时间，防止缓存雪崩
 */
public class RedisExpireUtil {

    /**
     * 默认的随机浮动范围（秒），默认 0-120秒
     */
    private static final int DEFAULT_JITTER_SECONDS = 1200;

    /**
     * 获取带有随机抖动的过期时间（单位：秒）
     * 逻辑：基础时间 + [0, 随机范围]
     *
     * @param baseSeconds 基础过期时间（秒），例如 300秒（5分钟）
     * @param jitterSeconds 随机浮动范围（秒），例如 60秒
     * @return 最终的过期时间（300 ~ 360 之间）
     */
    public static long getSeconds(long baseSeconds, int jitterSeconds) {
        if (jitterSeconds <= 0) {
            return baseSeconds;
        }
        // ThreadLocalRandom 在多线程环境下性能优于 Random
        long randomJitter = ThreadLocalRandom.current().nextLong(jitterSeconds + 1);
        return baseSeconds + randomJitter;
    }

    /**
     * 获取带有随机抖动的过期时间（使用默认抖动范围 60s）
     *
     * @param baseSeconds 基础过期时间（秒）
     * @return 最终过期时间
     */
    public static long getSeconds(long baseSeconds) {
        return getSeconds(baseSeconds, DEFAULT_JITTER_SECONDS);
    }

    /**
     * 获取带有随机抖动的 Duration 对象（Spring Data Redis 常用）
     *
     * @param baseSeconds 基础过期时间（秒）
     * @param jitterSeconds 随机浮动范围（秒）
     * @return Duration 对象
     */
    public static Duration getDuration(long baseSeconds, int jitterSeconds) {
        return Duration.ofSeconds(getSeconds(baseSeconds, jitterSeconds));
    }

}