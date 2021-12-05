package com.baidu.fsg.uid.common;

import java.time.Duration;

import com.baidu.fsg.uid.worker.WorkerIdAssignerStrategy;

/**
 * UidConsts
 *
 * @author gongxiaoyue
 */
public class UidConsts {
    /**
     * time bits
     */
    public static final int TIME_BITS = 40;
    /**
     * workerId bits
     */
    public static final int WORKER_BITS = 10;
    /**
     * serial number
     */
    public static final int SEQ_BITS = 13;

    /**
     * redis key
     */
    public static final String REDIS_KEY = "baidu-uid-generator";

    /**
     * last diff second reids key
     */
    public static final String WORKER_ID_REDIS_KEY = REDIS_KEY + "_WORKER_ID_KEY";


    /**
     * When the workerId is available, the difference is larger than the last time stored
     * second
     */
    public static final long AVAILABLE_WORKER_ID_INTERVAL_TIME = Duration.ofMinutes(30).getSeconds();

    /**
     * Strategy for assign workerId
     * */
    public static final WorkerIdAssignerStrategy WORKER_ID_ASSIGNER_STRATEGY = WorkerIdAssignerStrategy.LOOP;
}
