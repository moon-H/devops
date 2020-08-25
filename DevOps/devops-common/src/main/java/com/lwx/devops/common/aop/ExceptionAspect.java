package com.lwx.devops.common.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @description: 异常处理
 * @author: liwx
 * @create: 2019-11-27 16:19
 **/
//@Aspect
//@Component
public class ExceptionAspect {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final static String JOIN_POINT = "execution(public * com.huyan.demo.controller.AnalyticsController.*(..))";

    @Before(value = JOIN_POINT)
    public void before(JoinPoint joinPoint) {
        logger.warn("######## before==========");
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        StringBuilder params = new StringBuilder();
        for (Object arg : args) {
            params.append(arg).append(" ");
        }
        logger.info(className + "的" + methodName + "入参为：" + params.toString());
    }

    /**
     * 过程中监测，catch到异常之后返回包装后的错误信息，并打印日志
     */
    @Around(value = JOIN_POINT)
    public String catchException(ProceedingJoinPoint joinPoint) {
        try {
            logger.warn("######## 过程==========");
            Object result = joinPoint.proceed();
            return result.toString();
        } catch (Throwable e) {
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            logger.warn("在" + className + "的" + methodName + "中，发生了异常：" + e);
            return "1314";
        }
    }

    @AfterReturning(value = JOIN_POINT)
    public void afterReturning(JoinPoint joinPoint, Object returnVal) {
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        logger.info(className + "的" + methodName + "结果为：" + returnVal.toString());
    }

}
