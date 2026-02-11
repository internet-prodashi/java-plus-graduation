package ru.practicum.client;

import feign.FeignException;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CreateEndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.util.List;

@FeignClient(name = "stats-server")
public interface StatFeignClient {
    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    CreateEndpointHitDto createHit(@Valid @RequestBody CreateEndpointHitDto statDto) throws FeignException;

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    List<ViewStatsDto> getStats(@RequestParam(value = "start", required = false) String start,
                                @RequestParam(value = "end", required = false) String end,
                                @RequestParam(value = "uris", required = false) List<String> uris,
                                @RequestParam(value = "unique", required = false) Boolean unique)
            throws FeignException;
}