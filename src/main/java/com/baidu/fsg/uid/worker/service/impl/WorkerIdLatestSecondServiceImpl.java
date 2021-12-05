package com.baidu.fsg.uid.worker.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.baidu.fsg.uid.worker.dao.WorkerIdLatestSecondDao;
import com.baidu.fsg.uid.worker.entity.WorkerIdLatestSecondEntity;
import com.baidu.fsg.uid.worker.service.WorkerIdLatestSecondService;

import lombok.extern.slf4j.Slf4j;
import tk.mybatis.mapper.entity.Example;

/**
 * WorkerIdLastSecondServiceImpl
 *
 * @author gongxiaoyue
 */
@Service
@Slf4j
public class WorkerIdLatestSecondServiceImpl implements WorkerIdLatestSecondService {

    private static final String WORER_ID = "workerId";

    private static final String WORKER_NODE_ID = "workerNodeId";

    @Resource
    WorkerIdLatestSecondDao workerIdLatestSecondDao;

    @Override
    public int updateByWorkerId(WorkerIdLatestSecondEntity workerIdLatestSecondEntity) {
        if (validateEntityAndWorkerId(workerIdLatestSecondEntity)
                || workerIdLatestSecondEntity.getLastDiffSecond() == null) {
            return 0;
        }
        Example example = new Example(WorkerIdLatestSecondEntity.class);
        example.createCriteria().andEqualTo(WORER_ID, workerIdLatestSecondEntity.getWorkerId());
        return workerIdLatestSecondDao.updateByExampleSelective(workerIdLatestSecondEntity, example);
    }

    @Override
    public WorkerIdLatestSecondEntity getByWorkerId(Long workerId) {
        if (workerId == null) {
            return null;
        }
        Example example = new Example(WorkerIdLatestSecondEntity.class);
        example.createCriteria().andEqualTo(WORER_ID, workerId);
        return workerIdLatestSecondDao.selectOneByExample(example);
    }

    public WorkerIdLatestSecondEntity getByWorkerIdForUpdate(Long workerId) {
        if (workerId == null) {
            return null;
        }
        return workerIdLatestSecondDao.getByWorkerIdForUpdate(workerId);
    }

    @Override
    public boolean acquireLock(WorkerIdLatestSecondEntity workerIdLatestSecondEntity) {
        if (validateEntityAndWorkerId(workerIdLatestSecondEntity)
                || workerIdLatestSecondEntity.getLastDiffSecond() == null) {
            return false;
        }
        WorkerIdLatestSecondEntity dbEntity = this.getByWorkerId(workerIdLatestSecondEntity.getWorkerId());
        if (dbEntity == null) {
            return 1 == workerIdLatestSecondDao.insertSelective(workerIdLatestSecondEntity);
        } else {
            Example example = new Example(WorkerIdLatestSecondEntity.class);
            example.createCriteria().andEqualTo(WORER_ID, workerIdLatestSecondEntity.getWorkerId());
            example.createCriteria().andEqualTo(WORKER_NODE_ID, workerIdLatestSecondEntity.getWorkerNodeId());
            return 1 == workerIdLatestSecondDao.updateByExampleSelective(workerIdLatestSecondEntity, example);
        }
    }

    /**
     * Verify the legitimacy of WorkerIdLatestSecondEntity and workerId
     * @param workerIdLatestSecondEntity
     * @return
     */
    private boolean validateEntityAndWorkerId(WorkerIdLatestSecondEntity workerIdLatestSecondEntity) {
        return workerIdLatestSecondEntity == null || workerIdLatestSecondEntity.getWorkerId() == null;
    }

}
