package com.baidu.fsg.uid.worker.entity;

import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Tolerate;

/**
 * entity of db table  worker_id_latest_second
 *
 * @author gongxiaoyue
 */
@Builder
@Table(name = "worker_id_latest_second")
@ToString
@Data
public class WorkerIdLatestSecondEntity {

    @Tolerate
    public WorkerIdLatestSecondEntity() {

    }

    /**
     * Entity unique id (table unique)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * worker unique id
     */
    private Long workerId;

    /**
     * Maximum difference corresponding to workerid (seconds)
     */
    private Long lastDiffSecond;

    /**
     * worker_node unique id (table unique)
     */
    private Long workerNodeId;

    /**
     * remark information
     */
    private String remark;

    /**
     * Created time
     */
    private Date created;

    /**
     * Last modified
     */
    private Long modified;

}
