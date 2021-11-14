package com.baidu.fsg.uid.redis;

import java.util.Objects;

import javax.annotation.Resource;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * redis cache read and store
 * @author gongxiaoyue
 * @date 2021-11-08
 */
@Component
@Slf4j
public class RedisClient {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * hset
     * @param key
     * @param field
     * @param value
     */
    public void hset(String key, String field, String value) {
        try {
            stringRedisTemplate.opsForHash().put(key, field, value);
        } catch (Exception e) {
            log.error("RedisClient hset error.", e);
            throw e;
        }
    }

    /**
     * hget
     * @param key
     * @param field
     * @return
     */
    public String hget(String key, String field) {
        Object result = null;
        try {
            result = stringRedisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("RedisClient hget error.", e);
            throw e;
        }
        if (Objects.nonNull(result)) {
            return result.toString();
        }
        return null;
    }
}