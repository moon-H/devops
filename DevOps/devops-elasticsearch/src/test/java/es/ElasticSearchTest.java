package es;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.analyze.DetailAnalyzeResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: es  test
 * @author: liwx
 * @create: 2019-10-04 12:09
 **/
@RunWith(SpringRunner.class)
//@SpringBootTest(classes = EsApplication.class)
public class ElasticSearchTest {

    private static final String INDEX_KIBANA = "kibana_sample_data_ecommerce";
    private static final String INDEX_MOVIE = "movies";
    private static final String INDEX_LION = "index_lion";

    @Test
    public void indexRequest() {
        System.out.println("#### indexRequest 开始");
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("user", "kimchy");
        jsonMap.put("postDate", new Date());
        jsonMap.put("message", "trying out Elasticsearch");
        IndexRequest indexRequest = new IndexRequest(INDEX_LION)
                .id("1").source(jsonMap);
        System.out.println("#### indexRequest 結束");

        try {
            IndexResponse indexResponse = getHighLvClient().index(indexRequest, RequestOptions.DEFAULT);
            System.out.println("#### indexResponse " + indexResponse.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void analyze() {
        AnalyzeRequest request = new AnalyzeRequest();
        request.text("Some text to analyze", "Some more text to analyze");
        request.analyzer("english");
        try {
            AnalyzeResponse response = getHighLvClient().indices().analyze(request, RequestOptions.DEFAULT);
            DetailAnalyzeResponse detail = response.detail();
            List<AnalyzeResponse.AnalyzeToken> tokens=response.getTokens();
            System.out.println("123");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
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
                        new HttpHost("172.10.3.160", 9200, "http"),
                        new HttpHost("172.10.3.162", 9200, "http")));
        return client;
    }

}
