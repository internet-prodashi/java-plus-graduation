package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients(basePackages = {"ru.yandex.practicum.interaction.feign.clients", "ru.practicum.client"})
public class InteractionApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(InteractionApiApplication.class, args);
    }
}