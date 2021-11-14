package com.baidu.fsg.uid.worker.dao.base;


import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * DAO base class
 *
 * @author gongxiaoyue
 */
public interface BaseDao<T> extends Mapper<T>, MySqlMapper<T> {
}