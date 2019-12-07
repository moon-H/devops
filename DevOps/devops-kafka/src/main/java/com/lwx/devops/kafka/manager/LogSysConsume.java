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

    public static void main(String[] args) {

        Map<TopicPartition, Long> map = null;
        try {
            map = lagOf("test", "192.168.5.230:9092");
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
//        System.out.println("#### map" + map);
        if (map != null) {
            for (Map.Entry<TopicPartition, Long> a : map.entrySet()) {
//                System.out.println("topic = %s , partition = %s , LAG = %d" , a.getKey().topic(),a.getKey().partition(),a.getValue());
                System.out.printf("topic = %s, partition = %s, LAG = %s%n", a.getKey().topic(), a.getKey().partition(), a.getValue());
            }
        }

    }

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
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topicName));
//        consumer.subscribe(Arrays.asList("s1-audit-log"));
        while (true) {
            System.out.println(sdf.format(System.currentTimeMillis()) + "#######拉数据");
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
                    esManager.writeIndex(list);
                } catch (Exception e) {
                    logger.error("拉取数据异常",e);
                }
            }

            consumer.commitAsync(new OffsetCommitCallback() {
                @Override
                public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {

                }
            });


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


}
