package com.vaccine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VaccineApplication {

    public static void main(String[] args) {
        SpringApplication.run(VaccineApplication.class, args);
    }
}
