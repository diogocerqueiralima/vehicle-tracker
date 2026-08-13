package com.github.diogocerqueiralima.gateway.service.infrastructure.redis;

import com.github.diogocerqueiralima.gateway.service.domain.ports.outbound.RateLimitStore;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Redis-backed fixed-window counter. Chosen over an in-memory counter so limits are shared
 * consistently across every gateway instance running behind the load balancer.
 */
@Component
public class RedisRateLimitStore implements RateLimitStore {

    // 1. INCR + conditional EXPIRE run as a single Lua script so the increment and the
    //    window-start EXPIRE cannot race across concurrent requests hitting different gateway instances.
    private static final RedisScript<Long> INCREMENT_SCRIPT = RedisScript.of(
            """
            local current = redis.call('INCR', KEYS[1])
            if tonumber(current) == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            return current
            """,
            Long.class
    );

    private final ReactiveStringRedisTemplate redisTemplate;

    public RedisRateLimitStore(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Long> incrementAndGet(String key, int windowSeconds) {
        return redisTemplate.execute(INCREMENT_SCRIPT, List.of(key), List.of(String.valueOf(windowSeconds)))
                .single();
    }

}
