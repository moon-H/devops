package com.lwx.devops.elasticsearch;

import com.alibaba.fastjson.JSONObject;
import com.lwx.devops.elasticsearch.dao.IndexDao;
import com.lwx.devops.elasticsearch.logsystem.JsonDataProcessor;
import com.lwx.devops.elasticsearch.logsystem.SimpleBulkRequest;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: elasticsearch相关.索引管理，查询等
 * @author: liwx
 * @create: 2019-11-09 21:20
 **/
@Component
public class EsManager {
    private static Logger logger = LoggerFactory.getLogger(EsManager.class);

    private static final String INDEX_LION = "index_lion";
    private static final String INDEX_LOG_SYS = "log_info_test";
    private static final String INDEX_GRAFANA_TEXT1 = "index_grafana_test1";

    String strDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);

    @Resource
    IndexDao indexDao;

    public static void main(String[] args) {
        System.out.println("ES 测试开始");
        EsManager esManager = new EsManager();
        esManager.addData1("1001", "4401", "172.10.4.21", "login_1");
        esManager.addData1("1002", "4401", "172.10.4.21", "login_1");
        esManager.addData1("1003", "4402", "172.10.4.22", "login_2");
        esManager.addData1("1004", "4402", "172.10.4.22", "login_2");
        esManager.addData1("1005", "4403", "172.10.4.23", "login_2");
        esManager.addData1("1006", "4403", "172.10.4.23", "login_3");
        esManager.addData1("1007", "4403", "172.10.4.23", "login_3");
//        esManager.indexTestData();
//        esManager.indexTestData();
//        esManager.indexRequest();
    }


    private void indexTestData() {
        String date = sdf.format(System.currentTimeMillis());
        for (int i = 0; i < 3; i++) {
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("spend", Math.round(Math.random() * 1000));
            jsonMap.put("post_date", date);
            jsonMap.put("interface", "login_" + i);
            IndexRequest indexRequest = new IndexRequest(INDEX_LION)
                    .id(i + "").source(jsonMap);
            try {
                IndexResponse indexResponse = getHighLvClient().index(indexRequest, RequestOptions.DEFAULT);
                System.out.println("#### indexResponse " + indexResponse.toString());
            } catch (Exception e) {
                System.out.println("#### 创建索引异常 ");
                logger.error("#### 创建索引异常", e);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    public void indexRequest() {
        logger.debug("####@@@ indexRequest 开始");
        System.out.println("#### indexRequest 开始");
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("interface", "login");
        jsonMap.put("spend", Math.random() * 1000);
        jsonMap.put("post_date", sdf.format(System.currentTimeMillis()));
        IndexRequest indexRequest = new IndexRequest(INDEX_LION)
                .id(System.currentTimeMillis() + "").source(jsonMap);
        System.out.println("#### indexRequest 結束");

        try {
            IndexResponse indexResponse = getHighLvClient().index(indexRequest, RequestOptions.DEFAULT);
            System.out.println("#### indexResponse " + indexResponse.toString());

        } catch (Exception e) {
            System.out.println("#### 创建索引异常 ");
            logger.error("#### 创建索引异常", e);
        }

    }

    public void multiGet() {
        MultiGetRequest request = new MultiGetRequest();
        request.add(new MultiGetRequest.Item(
                INDEX_LION,
                "1"));
//        request.add(new MultiGetRequest.Item(INDEX_KIBANA, "zMQElW0B6_-ymSrM2QS7"));
        String[] includes = Strings.EMPTY_ARRAY;
//        String[] excludes = new String[]{"foo", "*r"};
//        FetchSourceContext fetchSourceContext =
//                new FetchSourceContext(true, includes, excludes);
        try {
            MultiGetResponse multiGetResponse = getHighLvClient().mget(request, RequestOptions.DEFAULT);
            MultiGetItemResponse[] itemResponses = multiGetResponse.getResponses();
            int length = multiGetResponse.getResponses().length;
            System.out.println("多条查询长度" + length);

            for (MultiGetItemResponse item : itemResponses) {
                System.out.println("#### INDEX=" + item.getResponse().getIndex() + " " + "#### source=" + item.getResponse().getSourceAsString());
            }
        } catch (ElasticsearchException e) {
            System.out.println("ES 异常=" + e.status());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private RestHighLevelClient getHighLvClient() {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("172.10.4.21", 9200, "http"),
                        new HttpHost("172.10.4.22", 9200, "http")));
        return client;
    }


    private void addData1(String id, String cityCode, String ip, String interfaceName) {
        String date = sdf.format(System.currentTimeMillis());
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("spend", Math.round(Math.random() * 1000));
        jsonMap.put("post_date", date);
        jsonMap.put("interface", interfaceName);
        jsonMap.put("cityCode", cityCode);
        jsonMap.put("ip", ip);
        IndexRequest indexRequest = new IndexRequest(INDEX_LION)
                .id(id).source(jsonMap);
        try {
            IndexResponse indexResponse = getHighLvClient().index(indexRequest, RequestOptions.DEFAULT);
            System.out.println("#### indexResponse " + indexResponse.toString());
        } catch (Exception e) {
            System.out.println("#### 创建索引异常 ");
            logger.error("#### 创建索引异常", e);
        }
    }


    public void writeIndex(Object entity) {
        logger.info("##### " + (indexDao == null));

        logger.info("线程[{}]", Thread.currentThread().getName());
        SimpleBulkRequest bulkRequest = new SimpleBulkRequest();
        String type = entity.getClass().getTypeName();
        if ("java.util.ArrayList".equals(type)) {
            List<JSONObject> list = (List<JSONObject>) entity;
            for (JSONObject jsonObject : list) {
                try {
                    jsonObject = JsonDataProcessor.processData(jsonObject);
                } catch (Exception e) {
                    logger.error("数据处理异常： " + jsonObject.toJSONString(), e);
                }
                String cityCode = jsonObject.getString("cityCode");
                IndexRequest indexRequest = new IndexRequest(INDEX_LOG_SYS).id(jsonObject.getString("id"))
                        .source(jsonObject.toJSONString(), XContentType.JSON);
                String routing = cityCode;
                indexRequest.routing(routing);
                bulkRequest.getDocIds().add(indexRequest.id());
                bulkRequest.add(indexRequest);
            }
        } else {
            JSONObject json = (JSONObject) entity;
            try {
                json = JsonDataProcessor.processData(json);
            } catch (Exception e) {
                logger.error("数据处理异常： " + json.toJSONString(), e);
            }
            String cityCode = json.getString("cityCode");
            IndexRequest indexRequest = new IndexRequest(INDEX_LOG_SYS).id(System.currentTimeMillis() + "")
                    .source(json.toJSONString(), XContentType.JSON);
            String routing = cityCode;
            indexRequest.routing(routing);
            bulkRequest.getDocIds().add(indexRequest.id());
            bulkRequest.add(indexRequest);
        }
        indexDao.bulk(bulkRequest);

        List<Integer> costBeforeTax = Arrays.asList(100, 200, 300, 400, 500);
        double total = 0;
        for (Integer cost : costBeforeTax) {
            double price = cost + .12*cost;
            total = total + price;
        }


    }

}
