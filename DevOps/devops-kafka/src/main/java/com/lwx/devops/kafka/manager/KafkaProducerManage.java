package com.lwx.devops.kafka.manager;

import com.google.gson.Gson;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * @description: kafka生产者
 * @author: liwx
 * @create: 2019-11-09 13:03
 **/
@Component
public class KafkaProducerManage {
    private static Logger logger = LoggerFactory.getLogger(KafkaProducerManage.class);
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public static final String TOPIC = "TOPIC_LOG_TEST";
    @Autowired
    private Gson gson;

    @Async
    public void sendTopic() {
        try {
            ListenableFuture<SendResult<String, Object>> future = kafkaTemplate.send(TOPIC, "kafka消息-" + System.currentTimeMillis());
            future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
                @Override
                public void onSuccess(SendResult<String, Object> result) {
                    RecordMetadata metadata = result.getRecordMetadata();
                    logger.debug("+++++kafka记录日志入队成功");
                }

                @Override
                public void onFailure(Throwable ex) {
                    logger.debug("+++++kafka记录日志入队异常" + ex);
                }

            });
        } catch (Exception e) {
            logger.debug("+++++kafka记录日志入队异常" + e);
        }
    }


}
