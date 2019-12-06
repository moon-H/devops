package com.lwx.devops.common.aop;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 测试Controller
 * @author: liwx
 * @create: 2019-11-27 11:23
 **/
@RestController
public class TestController {
    @RequestMapping(value = "/check")
    @AnalysisActuator
    public String checkTest(@Validated @RequestBody User user) {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "123";
    }

}
