package com.lwx.devops.app;

import com.lwx.devops.app.dao.PersionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @description: 测试kafka
 * @author: liwx
 * @create: 2019-11-09 12:50
 **/
@RestController
@RequestMapping("/app")
public class AppTestController {
    private static Logger logger = LoggerFactory.getLogger(AppTestController.class);
    //    @Resource
//    KafkaProducerManage kafkaProducerManage;
    @Resource
    PersionDao p;

    /**
     * kafka测试
     *
     * @param req
     * @return
     */
    @RequestMapping(value = "/producer", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String kafkaTest(@Valid String req) throws InterruptedException {
        logger.info("-----测试kafka开始-----------" + req);
//        kafkaProducerManage.sendTopic();
        logger.info("-----测试kafka结束-----------");
        logger.info("##### "+(p==null));
        p.add();
        return "OK";
    }

}
