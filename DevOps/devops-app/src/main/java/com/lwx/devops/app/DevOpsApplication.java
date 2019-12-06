package com.lwx.devops.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = {"com.lwx"})
@EnableAsync
public class DevOpsApplication {
    public static void main(String[] args) {
        SpringApplication.run(DevOpsApplication.class, args);
    }

}
