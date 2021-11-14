package com.baidu.fsg.uid.worker.workid;

import com.baidu.fsg.uid.exception.UidGenerateException;

/**
 * worker id checker service
 * Created by gongxiaoyue on 2021/11/07.
 */
public interface WorkerIdService {

    /**
     * Check worker id if it can be used
     *
     * @return worker id
     * @throws UidGenerateException exception
     */
    boolean checkWorkerIdAvailable(long workerId, long dbWorkerId) throws UidGenerateException;

    /**
     * take the remainder of a dbWorkId (database auto increment primary key)
     *
     * @param dbWorkerId Auto increment primary key
     * @return remainder
     */
    long generateWorkerId(Long dbWorkerId) throws UidGenerateException;
}
