package com.interviewgene;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for Resume Service
 */
@SpringBootApplication(scanBasePackages = {
    "com.interviewgene",
    "com.interviewgene.common"
})
@EntityScan(basePackages = {
    "com.interviewgene.model",
    "com.interviewgene.common"
})
@EnableJpaRepositories(basePackages = "com.interviewgene.repository")
@EnableTransactionManagement
public class ResumeServiceMain {

    public static void main(String[] args) {
        SpringApplication.run(ResumeServiceMain.class, args);
    }
}