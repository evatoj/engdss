package com.engss.transationService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TransationApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransationApplication.class, args);
    }

}