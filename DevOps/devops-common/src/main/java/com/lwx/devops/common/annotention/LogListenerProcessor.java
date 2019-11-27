package com.lwx.devops.common.annotention;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @description: LogListener处理
 * @author: liwx
 * @create: 2019-11-27 10:31
 **/
public class LogListenerProcessor extends AbstractProcessor {
    public void parseMethod(final Class<?> clazz) throws Exception {
        final Object obj = clazz.getConstructor(new Class[]{}).newInstance(new Object[]{});
        final Method[] methods = clazz.getDeclaredMethods();
        for (final Method method : methods) {
            final LogListener logListener = method.getAnnotation(LogListener.class);
            if (null != logListener) {
                method.invoke(obj, logListener.name());
            }
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }
}
