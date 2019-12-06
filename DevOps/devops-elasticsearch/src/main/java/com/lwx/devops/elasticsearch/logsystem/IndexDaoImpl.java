/*
 * Copyright (c) 2019, shankephone All Rights Reserved.
 * Project Name:log-system
 * Package Name:com.shankephone.log.dao
 * Date:2019/11/13 17:29
 *
 */

package com.lwx.devops.elasticsearch.logsystem;

import com.alibaba.fastjson.JSONObject;
import com.lwx.devops.elasticsearch.dao.IndexDao;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

/**
 * 类名:
 * 描述:
 * 时间: 2019/11/13 17:29
 *
 * @author fengql
 * @see
 * @since JDK 1.8
 */
@Service("indexDao")
public class IndexDaoImpl implements IndexDao {
    private Logger log = LoggerFactory.getLogger(IndexDaoImpl.class);

    //重试次数，3次
    private final static int RETRY_TIMES = 3;
    //重试间隔，1秒
    private final static int RETRY_INTERVAL = 1000;
    @Value("${indexer.servers}")
    private String indexerServers;
    @Override
    public boolean bulk(SimpleBulkRequest bulkRequest) {
        RestHighLevelClient client = RestClientFactory.buildClient(indexerServers);
        int count = bulkRequest.numberOfActions();
        if (count > 0) {
            Date startTime = new Date();
            try {
                client.bulk(bulkRequest, RequestOptions.DEFAULT);
				/*Date refreshStart = new Date();
				RefreshRequest request = new RefreshRequest(SECOND_INDEX_NAME);
				client.indices().refresh(request, RequestOptions.DEFAULT);*/
                /*log.info(">>>>>>> 批量创建索引：(" + count + ", " + bulkRequest.getDocIds() + ") "
                         + ",  索引耗时：" + (System.currentTimeMillis() - startTime.getTime())
                );*/
                log.info(">>>>>>> 批量创建索引：(" + count + ") "
                        + ",  索引耗时：" + (System.currentTimeMillis() - startTime.getTime())
                );
                return true;
            } catch (Exception e) {
                // 创建索引失败后重试3次，超过三次(每次间隔1秒)则抛出异常
                int i = 0;
                while (i++ < RETRY_TIMES) {
                    try {
                        Thread.sleep(RETRY_INTERVAL);
                        client.bulk(bulkRequest, RequestOptions.DEFAULT);
						/*Date refreshStart = new Date();
						RefreshRequest request = new RefreshRequest(SECOND_INDEX_NAME);
						client.indices().refresh(request, RequestOptions.DEFAULT);*/
                        log.warn("	重试批量创建索引(" + count + ")   耗时：" + (System.currentTimeMillis() - startTime.getTime()));
                        break;
                    } catch (Exception e2) {
                        if (BaseException.whichException(e2)) {
                            i = 0;
//                            ElasticSearchUtils.pingElasticsearch();// ping一下集群
                        }
                    }
                }
                if (i >= RETRY_TIMES) {
                    JSONObject json = new JSONObject();
                    json.put("docIds", "批量索引文档ID:" + bulkRequest.getDocIds().toString());
                    throw new BaseException("重试批量创建索引失败! 重试" + RETRY_TIMES + "次后跳过。 ", json);
                    //client.close();
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean delete(DeleteRequest request) {
        RestHighLevelClient client = RestClientFactory.buildClient(indexerServers);
        try {
            client.delete(request, RequestOptions.DEFAULT);
            return true;
        } catch (IOException e) {
            log.error("删除索引失败! docId: ", request.id());
        }
        return false;
    }

    @Override
    public boolean insert(IndexRequest request) {
        RestHighLevelClient client = RestClientFactory.buildClient(indexerServers);
        try {
            client.index(request, RequestOptions.DEFAULT);
            return true;
        } catch (IOException e) {
            log.error("新建索引失败! docId: ", request.id());
            return false;
        }
    }

    @Override
    public GetResponse get(GetRequest request) {
        RestHighLevelClient client = RestClientFactory.buildClient(indexerServers);
        try {
            return client.get(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("查询索引文档失败！ docId: " + request.id());
            return null;
        }
    }

    @Override
    public GetResponse get(String indexName, String id) {
        RestHighLevelClient client = RestClientFactory.buildClient(indexerServers);
        GetRequest get = new GetRequest(indexName, id);
        try {
            return client.get(get, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("查询索引文档失败！ docId: " + id);
            return null;
        }
    }

    @Override
    public boolean update(UpdateRequest request) {
        RestHighLevelClient client = RestClientFactory.buildClient(indexerServers);
        try {
            client.update(request, RequestOptions.DEFAULT);
            return true;
        } catch (IOException e) {
            log.error("更新索引文档失败！ docId: " + request.id());
            return false;
        }
    }

}