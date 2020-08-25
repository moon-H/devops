package com.lwx.devops.app;

import com.lwx.devops.kafka.manager.LogSysConsume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @description: 接收事件
 * @author: liwx
 * @create: 2019-12-06 15:15
 **/

@Component
public class MessageReceiver implements ApplicationListener<ApplicationReadyEvent> {
    private Logger logger = LoggerFactory.getLogger(MessageReceiver.class);
    @Autowired
    LogSysConsume logSysConsume;
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ConfigurableApplicationContext applicationContext = event.getApplicationContext();
        logger.info("###启动完成");

//        logSysConsume.pullData();
//        logSysConsume.injectTest();
    }
}

