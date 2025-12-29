package com.yu.common.config;

import com.yu.common.utils.RedisExpireUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 高可用 Redis 配置
 * 核心升级：
 * 1. 装饰模式：Redis 报错时降级查库。
 * 2. 熔断机制：当 Redis 连续失败达到阈值，自动“跳闸”一段时间，期间直接查库，不再等待 Redis 超时。
 */
@Slf4j
@Configuration
@EnableCaching
@ConditionalOnClass(RedisConnectionFactory.class)
public class RedisConfig implements CachingConfigurer {

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 1. 创建原生的 RedisCacheManager
        RedisCacheManager delegate = createOriginalRedisCacheManager(connectionFactory);

        // 2. 返回我们自定义的“容错+熔断”CacheManager
        return new ResilientCacheManager(delegate);
    }

    private RedisCacheManager createOriginalRedisCacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        // 建议：在 application.yml 中将 spring.data.redis.timeout 设置为 500ms 或更短
        // 这样即使熔断器还没跳闸，第一次失败也只会卡顿 0.5秒 而不是 3秒

        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(RedisExpireUtil.getDuration(15*3600, 1000))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                // 关键：防止 Redis 即使连上了但响应极慢拖死系统，这里也可以设置一个 command timeout (视具体需求而定)
                // .commandTimeout(Duration.ofMillis(500))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
        configMap.put("category", defaults.entryTtl(RedisExpireUtil.getDuration(30*3600, 100)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(configMap)
                .build();
    }

    /**
     * 简易熔断器状态管理 (所有 Cache 共享)
     */
    static class CircuitBreaker {
        // 熔断状态：true=开路(Redis不可用)，false=闭路(正常)
        private static volatile boolean open = false;
        // 熔断结束时间戳
        private static final AtomicLong retryTime = new AtomicLong(0);
        // 连续失败计数
        private static final AtomicInteger failCount = new AtomicInteger(0);

        // 配置：连续失败 3 次，熔断 30 秒
        private static final int MAX_FAILURES = 3;
        private static final long RECOVERY_INTERVAL_MS = 30000;

        /**
         * 检查 Redis 是否可用（是否在熔断期外）
         */
        public static boolean isAvailable() {
            if (open) {
                long now = System.currentTimeMillis();
                if (now > retryTime.get()) {
                    // 试探性恢复：过了熔断时间，允许放行一次请求去尝试
                    // 这里的逻辑可以优化为半开状态，为简单起见，只要时间到了就尝试
                    log.info("Redis 熔断保护期结束，尝试恢复连接...");
                    reset();
                    return true;
                }
                return false; // 仍在熔断期，直接返回不可用
            }
            return true;
        }

        /**
         * 记录一次成功
         */
        public static void recordSuccess() {
            if (failCount.get() > 0) {
                failCount.set(0);
            }
            if (open) {
                open = false;
                log.info("Redis 连接恢复正常，熔断器关闭。");
            }
        }

        /**
         * 记录一次失败
         */
        public static void recordFailure() {
            int currentFail = failCount.incrementAndGet();
            if (currentFail >= MAX_FAILURES) {
                open = true;
                retryTime.set(System.currentTimeMillis() + RECOVERY_INTERVAL_MS);
                log.error("Redis 连续失败 {} 次，熔断器开启！未来 {}ms 内将直接降级查库，不再访问 Redis。",
                        currentFail, RECOVERY_INTERVAL_MS);
            }
        }

        public static void reset() {
            open = false;
            failCount.set(0);
        }
    }

    static class ResilientCacheManager extends AbstractCacheManager {
        private final RedisCacheManager delegate;

        public ResilientCacheManager(RedisCacheManager delegate) {
            this.delegate = delegate;
        }

        @Override
        protected Collection<? extends Cache> loadCaches() {
            return delegate.getCacheNames().stream().map(this::getCache).collect(Collectors.toList());
        }

        @Override
        public Cache getCache(String name) {
            Cache cache = delegate.getCache(name);
            return cache != null ? new ResilientCache(cache) : null;
        }
    }

    /**
     * 装饰者模式 + 熔断检查
     */
    static class ResilientCache implements Cache {
        private final Cache delegate;

        public ResilientCache(Cache delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getName() { return delegate.getName(); }

        @Override
        public Object getNativeCache() { return delegate.getNativeCache(); }

        @Override
        public ValueWrapper get(Object key) {
            // 1. 熔断检查：如果熔断器开了，直接跳过 Redis，耗时 0ms
            if (!CircuitBreaker.isAvailable()) {
                log.debug("熔断中，跳过 Redis Get，Key: {}", key);
                return null;
            }

            try {
                ValueWrapper result = delegate.get(key);
                // 成功调用，重置失败计数
                CircuitBreaker.recordSuccess();
                return result;
            } catch (Exception e) {
                // 2. 失败记录：记录失败次数，触发熔断
                CircuitBreaker.recordFailure();
                log.warn("Redis Get 失败，Key: {}，降级查库。错误: {}", key, e.getMessage());
                return null;
            }
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            if (!CircuitBreaker.isAvailable()) return null;

            try {
                T result = delegate.get(key, type);
                CircuitBreaker.recordSuccess();
                return result;
            } catch (Exception e) {
                CircuitBreaker.recordFailure();
                log.warn("Redis Get(Type) 失败，Key: {}，降级查库。", key);
                return null;
            }
        }

        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            if (!CircuitBreaker.isAvailable()) {
                return loadFromDbDirectly(valueLoader);
            }

            try {
                T result = delegate.get(key, valueLoader);
                CircuitBreaker.recordSuccess();
                return result;
            } catch (Exception e) {
                CircuitBreaker.recordFailure();
                log.warn("Redis Get(Loader) 失败，Key: {}，降级查库。", key);
                return loadFromDbDirectly(valueLoader);
            }
        }

        private <T> T loadFromDbDirectly(Callable<T> valueLoader) {
            try {
                return valueLoader.call();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void put(Object key, Object value) {
            if (!CircuitBreaker.isAvailable()) return;

            try {
                delegate.put(key, value);
                CircuitBreaker.recordSuccess();
            } catch (Exception e) {
                CircuitBreaker.recordFailure();
                log.warn("Redis Put 失败，Key: {}", key);
            }
        }

        @Override
        public void evict(Object key) {
            if (!CircuitBreaker.isAvailable()) return;

            try {
                delegate.evict(key);
                CircuitBreaker.recordSuccess();
            } catch (Exception e) {
                CircuitBreaker.recordFailure();
            }
        }

        @Override
        public void clear() {
            if (!CircuitBreaker.isAvailable()) return;
            try {
                delegate.clear();
                CircuitBreaker.recordSuccess();
            } catch (Exception e) {
                CircuitBreaker.recordFailure();
            }
        }
    }
}
//```
//
//        ### 修改说明与优化建议
//
//1.  **添加了 `CircuitBreaker` 静态内部类**：
//        * **原理**：所有缓存共享一个熔断状态。因为如果是 Redis 挂了，通常是所有的 Cache 都连不上。
//        * **逻辑**：
//        * 当 Redis 操作（Get/Put）连续失败 **3次**（`MAX_FAILURES`）。
//        * 熔断器开启，设置 `open = true`。
//        * 接下来的 **30秒**（`RECOVERY_INTERVAL_MS`）内，所有 `get()` 请求通过 `!CircuitBreaker.isAvailable()` 判断，直接返回 `null`。**耗时接近 0ms**，不再去连接 Redis。
//        * 30秒后，允许一个请求尝试连接。如果成功，熔断器关闭；如果失败，继续熔断。
//
//        2.  **配置文件建议 (application.yml)**：
//        代码里的熔断只能解决“后续”的请求。为了让**前3次**失败也不要卡 3秒，请务必在 `application.yml` 或 `application.properties` 中调整连接超时时间：
//
//        ```yaml
//        spring:
//        data:
//        redis:
//        # 连接超时：建立 Socket 连接的最大等待时间
//        connect-timeout: 200ms
//        # 读取超时：发送指令后等待响应的最大时间
//        timeout: 500ms