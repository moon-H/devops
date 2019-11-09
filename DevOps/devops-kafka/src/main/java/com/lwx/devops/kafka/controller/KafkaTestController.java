package com.lwx.devops.kafka.controller;

import com.lwx.devops.DevOpsApplication;
import com.lwx.devops.kafka.manager.KafkaProducerManage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @description: 测试kafka
 * @author: liwx
 * @create: 2019-11-09 12:50
 **/
@RestController
@RequestMapping("/kafka")
public class KafkaTestController {
    private static Logger logger = LoggerFactory.getLogger(KafkaTestController.class);
    @Autowired
    KafkaProducerManage kafkaProducerManage;
    /**
     * kafka测试
     *
     * @param req
     * @return
     */
    @RequestMapping(value = "/producer", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String kafkaTest(@Valid String req) throws InterruptedException {
        logger.info("-----测试kafka开始-----------" + req);
        kafkaProducerManage.sendTopic();
        logger.info("-----测试kafka结束-----------");
        return "OK";
    }

}
