package com.baidu.fsg.uid.worker.workid;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.baidu.fsg.uid.ShutdownContext;
import com.baidu.fsg.uid.UidGenerator;
import com.baidu.fsg.uid.redis.DataCacheService;
import com.baidu.fsg.uid.utils.NamingThreadFactory;
import com.baidu.fsg.uid.worker.WorkerIdAssignerStrategy;
import com.baidu.fsg.uid.worker.entity.WorkerIdLatestSecondEntity;
import com.baidu.fsg.uid.worker.service.WorkerIdLatestSecondService;

/**
 *  Refresh the timestamp to the database or reids regularly, like heartbeat.
 *  both write databases and redis for high availability.
 *
 * @Author gongxiaoyue
 * @Date 2021-11-07
 */
@Component
public class TimestampScheduledTask implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimestampScheduledTask.class);

    private static final String SCHEDULE_NAME = "TimestampScheduledTask-Schedule";

    private static final long DEFAULT_SCHEDULE_INTERVAL = 10L; // 10 seconds

    private static final int TOLERATE_MAX_TIMES = 3;

    private final ScheduledExecutorService persistentTimestampSchedule =
            new ScheduledThreadPoolExecutor(1, new NamingThreadFactory(SCHEDULE_NAME));

    @Resource(name = "cachedUidGenerator")
    private UidGenerator uidGenerator;

    @Resource
    private WorkerIdLatestSecondService workerIdLatestSecondService;

    @Resource
    private ShutdownContext shutdownContext;

    @Resource
    private DataCacheService dataCacheService;

    @Value("${baidu.uid-generator.workerIdAssignerStrategyType:1}")
    private Integer workerIdAssignerStrategyType;

    private int tolerateTimes = 0;


    @Override
    public void afterPropertiesSet() throws Exception {
        if (WorkerIdAssignerStrategy.LOOP.equals(WorkerIdAssignerStrategy.valueOf(workerIdAssignerStrategyType))) {
            this.startTask();
            LOGGER.info("start persistent timestamp task successfully.");
        }
    }

    public void startTask() {
        // perform timestamp persistence every DEFAULT_SCHEDULE_INTERVAL seconds
        persistentTimestampSchedule.scheduleWithFixedDelay(() -> run(), DEFAULT_SCHEDULE_INTERVAL,
                DEFAULT_SCHEDULE_INTERVAL, TimeUnit.SECONDS);
    }

    public void run() {
        // set false if save to db failed
        boolean saveDb = Boolean.TRUE;
        // set false if save to redis failed
        boolean saveRedis = Boolean.TRUE;
        long timestamp = System.currentTimeMillis();
        try {
            // construct workerIdLastSecondEntity
            WorkerIdLatestSecondEntity workerIdLatestSecondEntity = new WorkerIdLatestSecondEntity();
            workerIdLatestSecondEntity.setLastDiffSecond(uidGenerator.getLastDiffSecond());
            workerIdLatestSecondEntity.setWorkerId(uidGenerator.getWorkerId());
            workerIdLatestSecondEntity.setModified(timestamp);
            // save to db
            workerIdLatestSecondService.updateByWorkerId(workerIdLatestSecondEntity);
        } catch (Exception e) {
            saveDb = Boolean.FALSE;
            LOGGER.error("TimestampScheduledTask write db fail:", e);
        }
        try {
            // save to redis
            dataCacheService.storeLatestTimestamp(uidGenerator.getWorkerId(), timestamp);
        } catch (Exception e) {
            saveRedis = Boolean.FALSE;
            LOGGER.error("TimestampScheduledTask write redis fail:", e);
        }
        if (!saveDb && !saveRedis) {
            LOGGER.error("timestamp write database and redis fail!");
            if (TOLERATE_MAX_TIMES == ++tolerateTimes) {
                LOGGER.error("timestamp write database and redis fail, showdown the server!");
                shutdownContext.showdown();
            }
        } else {
            // tolerateTimes  reset
            tolerateTimes = 0;
        }
        LOGGER.info("timestamp write database or redis success! workerId = {}, lastDiffSecond = {}",
                uidGenerator.getWorkerId(), uidGenerator.getLastDiffSecond());
    }
}
