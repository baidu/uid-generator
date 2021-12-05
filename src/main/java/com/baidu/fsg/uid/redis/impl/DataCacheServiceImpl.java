package com.baidu.fsg.uid.redis.impl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baidu.fsg.uid.common.UidConsts;
import com.baidu.fsg.uid.redis.DataCacheService;
import com.baidu.fsg.uid.redis.RedisClient;

/**
 * Represents an implementation of {@link DataCacheService}
 *
 * latest timestamp read and store
 *
 * @author gongxiaoyue
 */
@Service
public class DataCacheServiceImpl implements DataCacheService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataCacheServiceImpl.class);


    @Autowired
    private RedisClient redisClient;

    @Override
    public boolean storeLatestTimestamp(long workerId, long latestTimestamp) {
        if (workerId < 0 || latestTimestamp < 0) {
            return false;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("store data to redis!workerId:{},lastDiffSecond:{}", workerId, latestTimestamp);
        }
        redisClient.hset(UidConsts.WORKER_ID_REDIS_KEY, String.valueOf(workerId), String.valueOf(latestTimestamp));
        return true;
    }

    @Override
    public long getLatestTimestamp(long workerId) {
        if (workerId < 0) {
            return 0;
        }
        String latestTimestamp = redisClient.hget(UidConsts.WORKER_ID_REDIS_KEY, String.valueOf(workerId));
        if (StringUtils.isNotBlank(latestTimestamp)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("read data from redis!workerId:{},latestTimestamp:{}", workerId, latestTimestamp);
            }
            return Long.parseLong(latestTimestamp);
        }
        return 0;
    }
}
