package com.baidu.fsg.uid.worker;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baidu.fsg.uid.ShutdownContext;
import com.baidu.fsg.uid.utils.WorkerNodeUtils;
import com.baidu.fsg.uid.worker.entity.WorkerNodeEntity;
import com.baidu.fsg.uid.worker.service.WorkerNodeService;
import com.baidu.fsg.uid.worker.workid.WorkerIdService;

/**
 * Represents an implementation of {@link WorkerIdAssigner},
 * the worker id will be reused after assigned to the UidGenerator
 * the typical scenario is compatible front, because only 53 bit available for front,
 * so workerId need to be reused.
 *
 * @Author gongxiaoyue
 */
@Service("loopWorkerIdAssigner")
public class LoopWorkerIdAssigner implements WorkerIdAssigner {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoopWorkerIdAssigner.class);

    /**
     * retry count of get the workerId
     */
    private static final int GET_WORKERID_MAX_NUM = 5;

    @Resource
    private WorkerNodeService workerNodeService;

    @Resource
    ShutdownContext shutdownContext;

    @Resource
    WorkerIdService workerIdService;

    /**
     * Assign worker id base on database.<p>
     * If there is host name & port in the environment, we considered that the node runs in Docker container<br>
     * Otherwise, the node runs on an actual machine.
     *
     * @return assigned worker id
     */
    @Transactional(rollbackFor = Exception.class)
    public long assignWorkerId() {
        for (int i = 0; i < GET_WORKERID_MAX_NUM; i++) {
            Long workerId = tryAssignWorkerId();
            if (workerId == null) {
                continue;
            }
            LOGGER.info("assignWorkerId success!workerid:{}, num:{}", workerId, i);
            return workerId;
        }
        LOGGER.error("assignWorkerId all failed!close this instance!!!");
        // If the workid cannot be applied, the system will shut down
        shutdownContext.showdown();
        return 0;
    }

    /**
     * try assign a workerId
     */
    public Long tryAssignWorkerId() {
        // build worker node entity
        WorkerNodeEntity workerNodeEntity = WorkerNodeUtils.buildWorkerNode();

        // add worker node for new (ignore the same IP + PORT)
        workerNodeService.addWorkerNode(workerNodeEntity);
        LOGGER.info("Add worker node:" + workerNodeEntity);
        Long workerNodeId = workerNodeEntity.getId();
        if (null == workerNodeId) {
            LOGGER.warn("insert db and get id error!");
            return null;
        }
        long workerId = workerIdService.generateWorkerId(workerNodeId);
        if (workerIdService.checkWorkerIdAvailable(workerId, workerNodeId)) {
            return workerId;
        }
        return null;
    }

}
