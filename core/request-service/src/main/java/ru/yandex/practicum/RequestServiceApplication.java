package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RequestServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RequestServiceApplication.class, args);
    }
}
