package com.lwx.devops.kafka.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @description: kafka消费者
 * @author: liwx
 * @create: 2019-11-09 20:26
 **/
@Component
public class KafkaConsumerManage {
    private static Logger logger = LoggerFactory.getLogger(KafkaConsumerManage.class);

    @KafkaListener(topics = KafkaProducerManage.TOPIC, groupId = "group1")
    public void onMessage(String message) {
        //insertIntoDb(buffer);//这里为插入数据库代码
        logger.debug("### kafka消费消息 [{}]", message);

    }

    @KafkaListener(topics = KafkaProducerManage.TOPIC, groupId = "group2")
    public void onMessage2(String message) {
        //insertIntoDb(buffer);//这里为插入数据库代码
        logger.debug("### kafka消费消息2 [{}]", message);

    }
}
