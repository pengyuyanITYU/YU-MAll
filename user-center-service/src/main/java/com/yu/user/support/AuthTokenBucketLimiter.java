package com.yu.user.support;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class AuthTokenBucketLimiter {

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<List> tokenBucketScript;
    private final Map<String, TokenBucketRule> rules = Map.of(
            "challenge:global", new TokenBucketRule(60, Duration.ofMinutes(1)),
            "verify:global", new TokenBucketRule(60, Duration.ofMinutes(1)),
            "register:global", new TokenBucketRule(30, Duration.ofMinutes(1)),
            "register:username", new TokenBucketRule(3, Duration.ofMinutes(30)),
            "register:phone", new TokenBucketRule(3, Duration.ofMinutes(30)),
            "register:ip", new TokenBucketRule(10, Duration.ofMinutes(10)),
            "login:global", new TokenBucketRule(120, Duration.ofMinutes(1)),
            "login:username", new TokenBucketRule(10, Duration.ofMinutes(10)),
            "login:ip", new TokenBucketRule(30, Duration.ofMinutes(10))
    );

    public AuthTokenBucketLimiter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.tokenBucketScript = new DefaultRedisScript<>();
        this.tokenBucketScript.setLocation(new ClassPathResource("lua/token_bucket.lua"));
        this.tokenBucketScript.setResultType(List.class);
    }

    public TokenBucketResult tryConsume(String ruleName, String rawKey) {
        TokenBucketRule rule = rules.get(ruleName);
        if (rule == null) {
            throw new IllegalArgumentException("Unknown token bucket rule: " + ruleName);
        }
        List<Long> result = stringRedisTemplate.execute(
                tokenBucketScript,
                List.of("auth:bucket:" + ruleName + ":" + hashKey(rawKey)),
                String.valueOf(rule.capacity()),
                String.valueOf(rule.capacity()),
                String.valueOf(rule.refillPeriod().toMillis()),
                "1",
                String.valueOf(System.currentTimeMillis()),
                String.valueOf(rule.refillPeriod().multipliedBy(2).toMillis())
        );
        if (result == null || result.size() < 2) {
            return new TokenBucketResult(false, 0L);
        }
        return new TokenBucketResult(result.get(0) == 1L, result.get(1));
    }

    private String hashKey(String rawKey) {
        return DigestUtils.md5DigestAsHex(rawKey.getBytes(StandardCharsets.UTF_8));
    }

    private record TokenBucketRule(long capacity, Duration refillPeriod) {
    }

    public record TokenBucketResult(boolean allowed, long remainingTokens) {
    }
}
