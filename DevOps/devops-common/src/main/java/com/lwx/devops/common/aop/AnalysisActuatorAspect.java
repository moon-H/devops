package com.lwx.devops.common.aop;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.validation.Constraint;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法分析器
 */
@Aspect
@Component
public class AnalysisActuatorAspect {
    final static Logger log = LoggerFactory.getLogger(AnalysisActuatorAspect.class);
    ThreadLocal<Long> beginTime = new ThreadLocal<>();

    @Pointcut("@annotation(a)")
    public void serviceStatistics(AnalysisActuator a) {
        log.info("###### serviceStatistics");
    }

    @Before("serviceStatistics(analysisActuator)")
    public void doBefore(JoinPoint joinPoint, AnalysisActuator analysisActuator) {
        log.info("###### doBefore");
        // 记录请求到达时间
        beginTime.set(System.currentTimeMillis());
        log.info("method:{}", analysisActuator.methodName());
    }

    @After("serviceStatistics(analysisActuator)")
    public void doAfter(AnalysisActuator analysisActuator) {
        log.info("###### doAfter");
        log.info("method time:{}, name:{}", System.currentTimeMillis() - beginTime.get(), analysisActuator.methodName());
    }
}
