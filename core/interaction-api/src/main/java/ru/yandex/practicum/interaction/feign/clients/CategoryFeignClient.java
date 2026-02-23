package ru.yandex.practicum.interaction.feign.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.interaction.category.CategoryDto;
import ru.yandex.practicum.interaction.feign.config.FeignConfig;

@FeignClient(name = "category-service", configuration = {FeignConfig.class})
public interface CategoryFeignClient {
    @GetMapping("/categories/{cat-id}")
    CategoryDto getCategoryById(@PathVariable("cat-id") Long catId);
}