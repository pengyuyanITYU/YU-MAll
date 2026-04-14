package com.yu.user.support;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Component
public class AuthDistributedLock {

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> unlockScript;

    public AuthDistributedLock(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.unlockScript = new DefaultRedisScript<>();
        this.unlockScript.setLocation(new ClassPathResource("lua/unlock.lua"));
        this.unlockScript.setResultType(Long.class);
    }

    public LockToken tryLockRegister(String username, String phone, Duration ttl) {
        String key = "auth:lock:register:" + hashKey(username) + ":" + hashKey(phone);
        String value = UUID.randomUUID().toString();
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, value, ttl);
        if (Boolean.TRUE.equals(success)) {
            return new LockToken(key, value);
        }
        return null;
    }

    public void unlock(LockToken lockToken) {
        if (lockToken == null) {
            return;
        }
        stringRedisTemplate.execute(unlockScript, List.of(lockToken.key()), lockToken.value());
    }

    private String hashKey(String rawValue) {
        return DigestUtils.md5DigestAsHex(rawValue.getBytes(StandardCharsets.UTF_8));
    }

    public record LockToken(String key, String value) {
    }
}
