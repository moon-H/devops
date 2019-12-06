package com.lwx.devops.kafka.manager;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.stereotype.Component;

/**
 * @description: 异步处理
 * @author: liwx
 * @create: 2019-12-06 15:28
 **/
@Component
public class KafkaAsyncManager {

    public void processConsumeAsync(ConsumerRecords<String, String> records) {

    }
}
