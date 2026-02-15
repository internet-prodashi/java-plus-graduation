package ru.practicum.service;

import ru.practicum.dto.CreateEndpointHitDto;
import ru.practicum.dto.StatsRequest;
import ru.practicum.dto.ViewStatsDto;

import java.util.List;

public interface StatsService {
    void createHit(CreateEndpointHitDto createEndpointHitDto);

    List<ViewStatsDto> getStats(StatsRequest request);
}