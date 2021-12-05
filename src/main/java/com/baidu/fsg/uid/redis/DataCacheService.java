package com.baidu.fsg.uid.redis;

/**
 * Data cache read and store
 *
 * @author gongxiaoyue
 */
public interface DataCacheService {

    /**
     * cache workerId and latestTimestamp
     *
     * @param workerId         workerId
     * @param latestTimestamp latestTimestamp
     * @return boolean
     */
    boolean storeLatestTimestamp(long workerId, long latestTimestamp) throws Exception;

    /**
     * get the latestTimestamp by workerId
     * the latestTimestamp periodically cache to DB and redis
     *
     * @param workerId workerId
     * @return latestTimestamp
     */
    long getLatestTimestamp(long workerId);
}
