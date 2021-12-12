package com.baidu.fsg.uid.worker.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baidu.fsg.uid.worker.dao.base.BaseDao;
import com.baidu.fsg.uid.worker.entity.WorkerIdLatestSecondEntity;

/**
 * DAO for WorkIdLastSecond
 *
 * @author gongxiaoyue
 */
@Mapper
public interface WorkerIdLatestSecondDao extends BaseDao<WorkerIdLatestSecondEntity> {

    String TABLE = "worker_id_latest_second";

    @Select({"SELECT * FROM " + TABLE + " WHERE worker_id = #{workerId} FOR UPDATE"})
    WorkerIdLatestSecondEntity getByWorkerIdForUpdate(@Param("workerId") Long workerId);
}
