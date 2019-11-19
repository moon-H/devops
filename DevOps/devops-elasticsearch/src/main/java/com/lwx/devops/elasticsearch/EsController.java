package com.lwx.devops.elasticsearch;

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
@RequestMapping("/es")
public class EsController {
    private static Logger logger = LoggerFactory.getLogger(EsController.class);

    /**
     * kafka测试
     *
     * @param req
     * @return
     */
    @RequestMapping(value = "/elasticsearch", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String esTest(@Valid String req) throws InterruptedException {
        logger.info("-----测试Elastic search 开始-----------" + req);
        EsManager kafkaProducerManage = new EsManager();
        kafkaProducerManage.indexRequest();
        logger.info("-----测试Elastic search 结束-----------");
        return "OK";
    }

}
