package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "ru.practicum.client")
@SpringBootApplication
@ConfigurationPropertiesScan
public class StatApplication {
    public static void main(String[] args) {
        SpringApplication.run(StatApplication.class, args);
    }
}
