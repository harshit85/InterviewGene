package com.interviewgene;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestConfiguration;

/**
 * Test application for Resume Service tests
 */
@SpringBootApplication
@TestConfiguration
public class ResumeServiceTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResumeServiceTestApplication.class, args);
    }
}