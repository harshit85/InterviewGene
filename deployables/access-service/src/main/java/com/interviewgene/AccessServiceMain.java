package com.interviewgene;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


@SpringBootApplication(scanBasePackages = {"com.interviewgene"})
@EnableAutoConfiguration
public class AccessServiceMain extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(AccessServiceMain.class,args);
    }
}