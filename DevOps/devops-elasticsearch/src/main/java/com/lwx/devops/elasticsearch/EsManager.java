package com.lwx.devops.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * @description: elasticsearch相关.索引管理，查询等
 * @author: liwx
 * @create: 2019-11-09 21:20
 **/

public class EsManager {

    public static void main(String[] args) {
        System.out.println("ES 测试开始");
    }

    private RestHighLevelClient getHighLvClient() {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("172.10.4.21", 9200, "http"),
                        new HttpHost("172.10.4.22", 9200, "http")));
        return client;
    }
}
