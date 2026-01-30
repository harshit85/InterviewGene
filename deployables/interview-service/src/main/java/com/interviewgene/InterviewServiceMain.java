package com.interviewgene;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class InterviewServiceMain {
    public static void main(String[] args) {
        SpringApplication.run(InterviewServiceMain.class, args);
    }
}