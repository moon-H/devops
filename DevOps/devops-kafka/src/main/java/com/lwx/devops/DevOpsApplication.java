package com.lwx.devops;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DevOpsApplication {
    public static void main(String[] args) {
        SpringApplication.run(DevOpsApplication.class, args);
    }

}
