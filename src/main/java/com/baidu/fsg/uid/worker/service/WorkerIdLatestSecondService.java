package com.baidu.fsg.uid.worker.service;

import com.baidu.fsg.uid.worker.entity.WorkerIdLatestSecondEntity;

/**
 * WorkIdLastSecondService
 *
 * @author gongxiaoyue
 */
public interface WorkerIdLatestSecondService {

    /**
     * update WorkerIdLatestSecondEntity
     *
     * @param workerIdLatestSecondEntity
     * @return
     */
    int updateByWorkerId(WorkerIdLatestSecondEntity workerIdLatestSecondEntity) throws Exception;

    /**
     * get WorkIdLatestSecondEntity by workerId
     *
     * @param workerId
     * @return
     */
    WorkerIdLatestSecondEntity getByWorkerId(Long workerId);

    /**
     * get WorkIdLatestSecondEntity by workerId (have line lock)
     *
     * @param workerId
     * @return
     */
    WorkerIdLatestSecondEntity getByWorkerIdForUpdate(Long workerId);

    /**
     * get distributed lock from database for workerId
     *
     * @param workerIdLatestSecondEntity
     * @return if success return 1, otherwise return 0
     */
    boolean acquireLock(WorkerIdLatestSecondEntity workerIdLatestSecondEntity) throws Exception;
}
