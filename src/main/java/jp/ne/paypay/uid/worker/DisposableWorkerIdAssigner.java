/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserve.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.ne.paypay.uid.worker;

import jp.ne.paypay.uid.utils.DockerUtils;
import jp.ne.paypay.uid.utils.NetUtils;
import jp.ne.paypay.uid.worker.dao.WorkerNodeDAO;
import jp.ne.paypay.uid.worker.entity.WorkerNodeEntity;
import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Represents an implementation of {@link WorkerIdAssigner},
 * the worker id will be discarded after assigned to the UidGenerator
 *
 * @author yutianbao
 */
public class DisposableWorkerIdAssigner implements WorkerIdAssigner {
    private static final Logger LOGGER = LogManager.getLogger(DisposableWorkerIdAssigner.class);

    @Resource
    private WorkerNodeDAO workerNodeDAO;

    private final Connection connection;

    public DisposableWorkerIdAssigner(DataSource dataSource) throws SQLException {
        connection = dataSource.getConnection();
    }

    /**
     * Assign worker id base on database.<p>
     * If there is host name & port in the environment, we considered that the node runs in Docker container<br>
     * Otherwise, the node runs on an actual machine.
     *
     * @return assigned worker id
     */
    @Override
    public long assignWorkerId() {
        // build worker node entity
        WorkerNodeEntity workerNodeEntity = buildWorkerNode();

        try {
            connection.setAutoCommit(false);

            // add worker node for new (ignore the same IP + PORT)
            workerNodeDAO.addWorkerNode(workerNodeEntity);
            LOGGER.info("Add worker node:" + workerNodeEntity);

            connection.commit();
        }
        catch (SQLException ex) {
            try {
                connection.rollback();
            }
            catch (SQLException e) {
                LOGGER.error("Rollback failed.");
            }
        }

        return workerNodeEntity.getId();

    }

    /**
     * Build worker node entity by IP and PORT
     */
    private WorkerNodeEntity buildWorkerNode() {
        WorkerNodeEntity workerNodeEntity = new WorkerNodeEntity();
        if (DockerUtils.isDocker()) {
            workerNodeEntity.setType(WorkerNodeType.CONTAINER.value());
            workerNodeEntity.setHostName(DockerUtils.getDockerHost());
            workerNodeEntity.setPort(DockerUtils.getDockerPort());

        } else {
            workerNodeEntity.setType(WorkerNodeType.ACTUAL.value());
            workerNodeEntity.setHostName(NetUtils.getLocalAddress());
            workerNodeEntity.setPort(System.currentTimeMillis() + "-" + RandomUtils.nextInt(0, 100000));
        }

        return workerNodeEntity;
    }

}
