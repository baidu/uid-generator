package com.baidu.fsg.uid.worker.service;

import java.util.List;

import com.baidu.fsg.uid.worker.entity.WorkerNodeEntity;

/**
 * WorkerNodeService
 *
 * @author gongxiaoyue
 */
public interface WorkerNodeService {

    /**
     * add WorkerNodeEntity
     *
     * @param workerNodeEntity entity
     * @return int
     */
    int addWorkerNode(WorkerNodeEntity workerNodeEntity);

    /**
     * get by hostName and port
     *
     * @param hostname host
     * @param port     port
     * @return entity
     */
    List<WorkerNodeEntity> getWorkerNodeByHostPort(String hostname, String port);
}
