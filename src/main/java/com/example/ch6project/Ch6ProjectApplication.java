package com.example.ch6project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Ch6ProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(Ch6ProjectApplication.class, args);
    }

}
