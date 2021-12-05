package com.baidu.fsg.uid.worker.workid.impl;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.baidu.fsg.uid.UidGenerator;
import com.baidu.fsg.uid.common.UidConsts;
import com.baidu.fsg.uid.exception.UidGenerateException;
import com.baidu.fsg.uid.redis.DataCacheService;
import com.baidu.fsg.uid.worker.entity.WorkerIdLatestSecondEntity;
import com.baidu.fsg.uid.worker.service.WorkerIdLatestSecondService;
import com.baidu.fsg.uid.worker.workid.WorkerIdService;

/**
 * worker id checker service
 * Created by gongxiaoyue on 2021/11/09.
 */
@Service
public class WorkerIdServiceImpl implements WorkerIdService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerIdServiceImpl.class);

    @Resource
    DataCacheService dataCacheService;

    @Resource
    WorkerIdLatestSecondService workerIdLatestSecondService;

    @Resource
    UidGenerator uidGenerator;

    @Override
    public boolean checkWorkerIdAvailable(long workerId, long workerNodeId) throws UidGenerateException {
        if (workerId < 0) {
            return false;
        }
        // Get cached lastSecond by workerId
        long latestTimestamp = getMaxLatestTimestamp(workerId);

        // If lastDiffSecond is 0,Indicates that it has not been used then return true
        if (latestTimestamp == 0) {
            return tryAcquireLock(workerId, workerNodeId);
        }
        Long latestDiffSecond = TimeUnit.MILLISECONDS.toSeconds(latestTimestamp);

        // get the current time (second)
        long currentSecond = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        LOGGER.info("checkWorkerIdAvailable!currentSecond:{},latestDiffSecond:{}", currentSecond, latestDiffSecond);

        // If the difference is greater than the set interval time, returns true, otherwise returns false
        if (currentSecond - latestDiffSecond > UidConsts.AVAILABLE_WORKER_ID_INTERVAL_TIME) {
            return tryAcquireLock(workerId, workerNodeId);
        }
        return false;
    }

    private boolean tryAcquireLock(long workerId, long workerNodeId) {
        try {
            WorkerIdLatestSecondEntity workerIdLatestSecondEntity = WorkerIdLatestSecondEntity.builder()
                    .workerId(workerId).workerNodeId(workerNodeId).modified(System.currentTimeMillis()).build();
            if (!workerIdLatestSecondService.acquireLock(workerIdLatestSecondEntity)) {
                LOGGER.warn("WorkerIdServiceImpl checkWorkerIdAvailable acquire lock fail!");
                return false;
            }
            return true;
        } catch (Exception e) {
            LOGGER.warn("WorkerIdServiceImpl checkWorkerIdAvailable acquire lock fail!", e);
            return false;
        }
    }

    /**
     * get max latestTimestamp from db and redis
     * run at startup
     *
     */
    private long getMaxLatestTimestamp(long workerId) {
        // get data from redis
        long redisDate = dataCacheService.getLatestTimestamp(workerId);
        // get data from db
        WorkerIdLatestSecondEntity entity = workerIdLatestSecondService.getByWorkerIdForUpdate(workerId);
        if (entity == null || entity.getModified() == null) {
            return redisDate;
        }
        return Math.max(redisDate, entity.getModified());
    }

    @Override
    public long generateWorkerId(Long workerNodeId) throws UidGenerateException {
        if (workerNodeId == null || workerNodeId < 0) {
            throw new UidGenerateException("dbWorkerId is negative number");
        }
        Long maxWorkerId = ~(-1L << uidGenerator.getWorkerBits()) + 1;
        return workerNodeId.intValue() % maxWorkerId.intValue();
    }
}
