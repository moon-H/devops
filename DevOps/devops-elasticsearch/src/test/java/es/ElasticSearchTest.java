package es;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lwx.devops.elasticsearch.dao.Car;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.analyze.DetailAnalyzeResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @description: es  test
 * @author: liwx
 * @create: 2019-10-04 12:09
 **/
@RunWith(SpringRunner.class)
@Slf4j
//@SpringBootTest(classes = EsApplication.class)
public class ElasticSearchTest {

    private static final String INDEX_KIBANA = "kibana_sample_data_ecommerce";
    private static final String INDEX_MOVIE = "movies";
    private static final String INDEX_LION = "index_lion";
    private static final String INDEX_CARS = "cars";

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
    public void indexData() throws ParseException {
        List<Car> carList = getCarListMap();
        for (Car car : carList) {
            String carString = JSONObject.toJSONString(car);
            IndexRequest indexRequest = new IndexRequest(INDEX_CARS)
//                .id("1")
                    .source(carString, XContentType.JSON);
            try {
                IndexResponse indexResponse = getHighLvClient().index(indexRequest, RequestOptions.DEFAULT);
                log.info("#### indexResponse[{}]", indexResponse.toString());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Car> getCarListMap() throws ParseException {
        SimpleDateFormat sb = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

        Car car1 = new Car();
        car1.setMake("奔驰");
        car1.setColor("黄色");
        car1.setPrice(10000L);
        car1.setSold_date(sb.format(sb.parse("2020-06-01 00:00:00")));

        Car car2 = new Car();
        car2.setMake("奔驰");
        car2.setColor("黄色");
        car2.setPrice(20000L);
        car2.setSold_date(sb.format(sb.parse("2020-06-01 00:00:00")));
//
        Car car3 = new Car();
        car3.setMake("宝马");
        car3.setColor("红色");
        car3.setPrice(25000L);
        car3.setSold_date(sb.format(sb.parse("2020-07-01 00:00:00")));
//
        Car car4 = new Car();
        car4.setMake("法拉利");
        car4.setColor("红色");
        car4.setPrice(55000L);
        car4.setSold_date(sb.format(sb.parse("2020-08-01 00:00:00")));

        List<Car> carList = new ArrayList<>();
        carList.add(car1);
        carList.add(car2);
        carList.add(car3);
        carList.add(car4);


        return carList;
    }


    @Test
    public void analyze() {
        AnalyzeRequest request = new AnalyzeRequest();
        request.text("Some text to analyze", "Some more text to analyze");
        request.analyzer("english");
        try {
            AnalyzeResponse response = getHighLvClient().indices().analyze(request, RequestOptions.DEFAULT);
            DetailAnalyzeResponse detail = response.detail();
            List<AnalyzeResponse.AnalyzeToken> tokens = response.getTokens();
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

    @Test
    public void aggregationTest() {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("by_company").field("company.keyword").size(100);
        sourceBuilder.aggregation(termsAggregationBuilder);
        sourceBuilder.size(0);
        log.info("sourceBuilder [{}]", sourceBuilder.toString());

        SearchRequest searchRequest = new SearchRequest(INDEX_CARS);
        searchRequest.source(sourceBuilder);
        log.info("searchRequest [{}]", searchRequest.toString());
        try {
            SearchResponse searchResponse = getHighLvClient().search(searchRequest, RequestOptions.DEFAULT);
            RestStatus status = searchResponse.status();
            log.info("searchResponse [{}]", searchResponse.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }


        //        getHighLvClient().searchAsync(searchRequest, RequestOptions.DEFAULT, new ActionListener<SearchResponse>() {
//            @Override
//            public void onResponse(SearchResponse searchResponse) {
//
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//
//            }
//        });

    }


    private RestHighLevelClient getHighLvClient() {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
//                        new HttpHost("172.10.3.160", 9200, "http"),
//                        new HttpHost("172.10.3.162", 9200, "http")));
                        new HttpHost("172.10.4.21", 9200, "http")));
        return client;
    }

}
