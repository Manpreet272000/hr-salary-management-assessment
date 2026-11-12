package com.acme.hr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HrSalaryManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrSalaryManagementApplication.class, args);
    }
}
