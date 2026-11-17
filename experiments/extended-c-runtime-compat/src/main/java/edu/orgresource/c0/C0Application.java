package edu.orgresource.c0;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class C0Application {
    public static void main(String[] args) {
        SpringApplication.run(C0Application.class, args);
    }
}
