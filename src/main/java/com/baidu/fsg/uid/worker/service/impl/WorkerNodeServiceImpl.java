package com.baidu.fsg.uid.worker.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.baidu.fsg.uid.worker.dao.WorkerNodeDao;
import com.baidu.fsg.uid.worker.entity.WorkerNodeEntity;
import com.baidu.fsg.uid.worker.service.WorkerNodeService;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;
import tk.mybatis.mapper.entity.Example;

/**
 * WorkerNodeServiceImpl
 *
 * @author gongxiaoyue
 */
@Service
@Slf4j
public class WorkerNodeServiceImpl implements WorkerNodeService {

    @Resource
    WorkerNodeDao workerNodeDao;

    @Override
    public int addWorkerNode(WorkerNodeEntity workerNodeEntity) {
        if (workerNodeEntity == null) {
            return 0;
        }
        return workerNodeDao.insertSelective(workerNodeEntity);
    }

    @Override
    public List<WorkerNodeEntity> getWorkerNodeByHostPort(String hostName, String port) {
        if (StringUtils.isBlank(hostName) || StringUtils.isBlank(port)) {
            return Lists.newArrayList();
        }
        Example example = new Example(WorkerNodeEntity.class);
        example.createCriteria().andEqualTo("hostName", hostName)
                .andEqualTo("port", port);
        return workerNodeDao.selectByExample(example);
    }
}
