package com.baidu.fsg.uid.utils;

import org.apache.commons.lang.math.RandomUtils;

import com.baidu.fsg.uid.worker.WorkerNodeType;
import com.baidu.fsg.uid.worker.entity.WorkerNodeEntity;
/**
 * util for WorkerNodeEntity
 *
 * @Author gongxiaoyue
 */
public class WorkerNodeUtils {

    /**
     * Build worker node entity by IP and PORT
     */
    public static WorkerNodeEntity buildWorkerNode() {
        WorkerNodeEntity workerNodeEntity = new WorkerNodeEntity();
        if (DockerUtils.isDocker()) {
            workerNodeEntity.setType(WorkerNodeType.CONTAINER.value());
            workerNodeEntity.setHostname(DockerUtils.getDockerHost());
            workerNodeEntity.setPort(DockerUtils.getDockerPort());
        } else {
            workerNodeEntity.setType(WorkerNodeType.ACTUAL.value());
            workerNodeEntity.setHostname(NetUtils.getLocalAddress());
            workerNodeEntity.setPort(System.currentTimeMillis() + "-" + RandomUtils.nextInt(100000));
        }
        return workerNodeEntity;
    }
}
