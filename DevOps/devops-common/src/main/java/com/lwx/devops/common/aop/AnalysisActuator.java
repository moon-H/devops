package com.lwx.devops.common.aop;


import javax.validation.Constraint;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义验证注解
 */
@Target({ElementType.METHOD}) //只允许用在类上
@Retention(RetentionPolicy.RUNTIME) //注解保留在程序运行期间，此时可以通过反射获得定义在某个类上的所有注解
public @interface AnalysisActuator {
    /**
     * 方法名
     */
    String methodName() default "未指定方法名";
}
