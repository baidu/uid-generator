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
package com.github.wujun234.uid.worker.dao;

import com.github.wujun234.uid.worker.entity.WorkerNodeEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * DAO for M_WORKER_NODE
 *
 * @author yutianbao
 * @author wujun
 */
@Repository
public interface WorkerNodeDAO {

    /**
     * Get {@link WorkerNodeEntity} by node host
     * 
     * @param host
     * @param port
     * @return
     */
    @Select("SELECT " +
            " ID," +
            " HOST_NAME," +
            " PORT," +
            " TYPE," +
            " LAUNCH_DATE," +
            " MODIFIED," +
            " CREATED" +
            " FROM" +
            " WORKER_NODE" +
            " WHERE" +
            " HOST_NAME = #{host,jdbcType=VARCHAR} AND PORT = #{port,jdbcType=VARCHAR}")
    WorkerNodeEntity getWorkerNodeByHostPort(@Param("host") String host, @Param("port") String port);

    /**
     * Add {@link WorkerNodeEntity}
     * 
     * @param workerNodeEntity
     */
    @Insert("INSERT INTO WORKER_NODE" +
            "(HOST_NAME," +
            "PORT," +
            "TYPE," +
            "LAUNCH_DATE," +
            "MODIFIED," +
            "CREATED)" +
            "VALUES (" +
            "#{hostName}," +
            "#{port}," +
            "#{type}," +
            "#{launchDate}," +
            "NOW()," +
            "NOW())")
    @Options(useGeneratedKeys = true, keyColumn = "id")
    void addWorkerNode(WorkerNodeEntity workerNodeEntity);

}
