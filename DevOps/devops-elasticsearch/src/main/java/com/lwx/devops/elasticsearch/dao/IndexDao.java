/*
 * Copyright (c) 2019, shankephone All Rights Reserved.
 * Project Name:log-system
 * Package Name:com.shankephone.log.dao
 * Date:2019/11/13 17:29
 *
 */

package com.lwx.devops.elasticsearch.dao;

import com.lwx.devops.elasticsearch.dao.BaseDao;
import com.lwx.devops.elasticsearch.logsystem.SimpleBulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.springframework.stereotype.Component;

/**
 * 接口:
 * 描述:
 * 时间: 2019/11/13 17:29
 *
 * @author fengql
 * @see
 * @since JDK 1.8
 */
public interface IndexDao extends BaseDao {

    /**
     * 批量创建索引
     * @param bulkRequest
     */
    boolean bulk(SimpleBulkRequest bulkRequest);

    /**
     * 删除索引
     * @param request
     */
    boolean delete(DeleteRequest request);

    /**
     * 新增索引
     * @param request
     */
    boolean insert(IndexRequest request);

    /**
     * 获取索引
     * @param request
     */
    GetResponse get(GetRequest request);

    /**
     * 获取索引
     * @param indexName
     * @param id
     */
    GetResponse get(String indexName, String id);

    /**
     * 更新索引
     * @param request
     */
    boolean update(UpdateRequest request);

}
