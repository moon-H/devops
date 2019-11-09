package com.lwx.devops.kafka.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@Configuration
@EnableAsync
public class AsyncTaskConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
         //线程池
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        //初始化线程数
        taskExecutor.setCorePoolSize(500);
        //最大线程数
        taskExecutor.setMaxPoolSize(500);
        //线程队列长度
        taskExecutor.setQueueCapacity(100);
        taskExecutor.setKeepAliveSeconds(3000);
        // rejection-policy：当pool已经达到max size的时候，如何处理新任务 
        // CALLER_RUNS：不在新线程中执行任务，而是由调用者所在的线程来执行 
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); 
        taskExecutor.setThreadNamePrefix("AsyncThread-");
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
         return null;
    }
}
