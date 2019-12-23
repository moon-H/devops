package com.lwx.devops.kafka.manager;

import com.alibaba.fastjson.JSONObject;
import com.lwx.devops.elasticsearch.EsManager;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @description: 异步处理kafka消息
 * @author: liwx
 * @create: 2019-12-07 17:45
 **/
@Component
public class AsyncConsume {
    private Logger logger = LoggerFactory.getLogger(AsyncConsume.class);

    @Resource
    EsManager esManager;
    @Resource
    AsyncConfigurer asyncConfigurer;

    public void asyncConsumeLog(KafkaConsumer<String, String> consumer, List<JSONObject> list) {
//        logger.info("线程[]", Thread.currentThread().getName());
        Executor executor = asyncConfigurer.getAsyncExecutor();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                esManager.writeIndex(list);
            }
        });
        consumer.commitAsync(new OffsetCommitCallback() {
            @Override
            public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {

            }
        });
    }

}
