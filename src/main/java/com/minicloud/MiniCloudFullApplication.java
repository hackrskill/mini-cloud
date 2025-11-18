package com.minicloud;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.minicloud")
@EnableScheduling
@EnableAsync
public class MiniCloudFullApplication {
    public static void main(String[] args) {
        SpringApplication.run(MiniCloudFullApplication.class, args);
    }
}
