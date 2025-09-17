package com.evolforge.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.evolforge.core")
public class RielHomeApplication {

    public static void main(String[] args) {
        SpringApplication.run(RielHomeApplication.class, args);
    }
}
