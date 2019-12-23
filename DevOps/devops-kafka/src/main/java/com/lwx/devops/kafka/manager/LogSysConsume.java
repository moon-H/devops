package com.lwx.devops.kafka.manager;

import com.alibaba.fastjson.JSONObject;
import com.lwx.devops.elasticsearch.EsManager;
import com.lwx.devops.elasticsearch.dao.IndexDao;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * @description: kafka消费者测试
 * @author: liwx
 * @create: 2019-12-04 16:31
 **/
@Component
public class LogSysConsume {
    private Logger logger = LoggerFactory.getLogger(LogSysConsume.class);
    //    EsManager esManager = new EsManager();
    @Resource
    IndexDao indexDao;
    @Resource
    EsManager esManager;
    @Value("${kafka.topic.name}")
    private String topicName;
    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${kafka.consumer.group.id}")
    private String groupId;
    @Value("${kafka.consumer.auto.offset.reset}")
    private String consumerAutoOffsetReset;

    @Resource
    private AsyncConsume asyncConsume;

    public void injectTest() {
        logger.info("##### " + (indexDao == null));
    }

    public void pullData() {
        logger.info("###pullData start");
        String strDateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);

        System.out.printf(sdf.format(System.currentTimeMillis()) + "#############Start");

        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", groupId);
        props.put("enable.auto.commit", "false");
        props.put("auto.commit.interval.ms", "500");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerAutoOffsetReset);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,6);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topicName));
//        consumer.subscribe(Arrays.asList("s1-audit-log"));
        while (true) {
            logger.debug("#######拉数据");
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
            List<JSONObject> list = new ArrayList<JSONObject>();
            if (records.count() == 0) continue;
            for (ConsumerRecord<String, String> record : records) {
                logger.debug("## 消费数据 offset ={}, key = {}, value = {}", record.offset(), record.key(), record.value());
                String key = record.key();
                String topic = record.topic();
                String value = record.value();
                //log.info("接收数据：" + value);
                try {
                    JSONObject json = JSONObject.parseObject(value);
                    list.add(json);
                } catch (Exception e) {
                    logger.error("拉取数据异常", e);
                }
            }
            logger.info("拉取到的数据[{}]", list.size());
            asyncConsume.asyncConsumeLog(consumer, list);
            //            try {
//                Thread.sleep(1000*10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
    }


    public static Map<TopicPartition, Long> lagOf(String groupID, String bootstrapServers) throws TimeoutException {
        Properties props = new Properties();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        try (AdminClient client = AdminClient.create(props)) {
            ListConsumerGroupOffsetsResult result = client.listConsumerGroupOffsets(groupID);
            try {
                Map<TopicPartition, OffsetAndMetadata> consumedOffsets = result.partitionsToOffsetAndMetadata().get(10, TimeUnit.SECONDS);
                props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // 禁止自动提交位移
                props.put(ConsumerConfig.GROUP_ID_CONFIG, groupID);
                props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
                props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
                try (final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
                    Map<TopicPartition, Long> endOffsets = consumer.endOffsets(consumedOffsets.keySet());
                    return endOffsets.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(),
                            entry -> entry.getValue() - consumedOffsets.get(entry.getKey()).offset()));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // 处理中断异常
                // ...
                return Collections.emptyMap();
            } catch (ExecutionException e) {
                // 处理ExecutionException
                // ...
                return Collections.emptyMap();
            } catch (TimeoutException e) {
                throw new TimeoutException("Timed out when getting lag for consumer group " + groupID);
            }
        }
    }

    public static void main(String[] args) {

//        Map<TopicPartition, Long> map = null;
//        try {
//            map = lagOf("test", "192.168.5.230:9092");
//        } catch (TimeoutException e) {
//            e.printStackTrace();
//        }
////        System.out.println("#### map" + map);
//        if (map != null) {
//            for (Map.Entry<TopicPartition, Long> a : map.entrySet()) {
////                System.out.println("topic = %s , partition = %s , LAG = %d" , a.getKey().topic(),a.getKey().partition(),a.getValue());
//                System.out.printf("topic = %s, partition = %s, LAG = %s%n", a.getKey().topic(), a.getKey().partition(), a.getValue());
//            }
//        }

//        ElasticSearchUtils.pingElasticsearch();//ping一下集群
//        LogFileCleanTask.clean();
//        LogIndexerExecutor executor =  new LogIndexerExecutor();
//        executor.execute();
            String data = "{\"timestamp\":\"2019-12-11 19:25:49.892\",\"serverName\":\"S1\",\"handlerServerIp\":\"172.10.2.73\",\"cityCode\":\"1500\",\"thread\":\"http-nio-8180-exec-27\",\"level\":\"INFO \",\"msg\":\"{\\\"requestLoggingMessage\\\":{\\\"heading\\\":\\\"Inbound Message\\\\n* * * * * * * * * * * * * * * * * * * * * * * *\\\",\\\"id\\\":\\\"1694\\\",\\\"address\\\":\\\"http://192.168.5.209:8180/sttrade/ci/tvm/notiDeviceHeard\\\",\\\"contentType\\\":\\\"application/x-www-form-urlencoded\\\",\\\"encoding\\\":\\\"\\\",\\\"httpMethod\\\":\\\"POST\\\",\\\"header\\\":\\\"{content-length\\\\u003d[94], host\\\\u003d[192.168.5.209:8180], connection\\\\u003d[keep-alive], content-type\\\\u003d[application/x-www-form-urlencoded], user-agent\\\\u003d[Apache-HttpClient/4.5.10 (Java/1.8.0_73)]}\\\",\\\"message\\\":\\\"\\\",\\\"payload\\\":\\\"charset\\\\u003dUTF-8\\\\u0026providerId\\\\u003d04\\\\u0026format\\\\u003djson\\\\u0026signType\\\\u003d00\\\\u0026deviceId\\\\u003d02310803\\\\u0026timestamp\\\\u003d20191106104217\\\",\\\"responseCode\\\":\\\"\\\",\\\"processingTime\\\":\\\"\\\",\\\"handlerDate\\\":\\\"2019-12-11 07:25:49.832\\\",\\\"deviceId\\\":\\\"02310803\\\",\\\"providerId\\\":\\\"04\\\"},\\\"responseLoggingMessage\\\":{\\\"heading\\\":\\\"Outbound Message\\\\n* * * * * * * * * * * * * * * * * * * * * * * *\\\",\\\"id\\\":\\\"1694\\\",\\\"address\\\":\\\"\\\",\\\"contentType\\\":\\\"application/json;charset\\\\u003dUTF-8\\\",\\\"encoding\\\":\\\"\\\",\\\"httpMethod\\\":\\\"\\\",\\\"header\\\":\\\"{X-Application-Context\\\\u003d[application:150000001-dev:8180], Content-Type\\\\u003d[application/json;charset\\\\u003dUTF-8]}\\\",\\\"message\\\":\\\"\\\",\\\"payload\\\":\\\"{\\\\\\\"retCode\\\\\\\":\\\\\\\"0000\\\\\\\",\\\\\\\"retMsg\\\\\\\":\\\\\\\"成功\\\\\\\"}\\\",\\\"responseCode\\\":\\\"200\\\",\\\"processingTime\\\":\\\"4\\\",\\\"handlerDate\\\":\\\"2019-12-11 07:25:49.836\\\",\\\"deviceId\\\":\\\"\\\",\\\"providerId\\\":\\\"\\\"},\\\"cityCode\\\":\\\"1500\\\",\\\"requestUrl\\\":\\\"/sttrade/ci/tvm/notiDeviceHeard\\\",\\\"clientAddress\\\":\\\"192.168.2.113\\\",\\\"method\\\":\\\"POST\\\",\\\"servletName\\\":\\\"\\\",\\\"statusCode\\\":-1,\\\"userName\\\":\\\"\\\",\\\"processingTimeMillis\\\":4,\\\"timestamp\\\":1576063549837}\",\"logger\":\"com.panchan.its.tvm.filter.MsgUtil\"}";

            System.out.println("长度=" + data.getBytes().length);


    }
}
