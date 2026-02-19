package ru.yandex.practicum.events.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatFeignClient;
import ru.practicum.dto.ViewStatsDto;
import ru.yandex.practicum.events.mapper.EventMapper;
import ru.yandex.practicum.events.model.Event;
import ru.yandex.practicum.events.repository.EventRepository;
import ru.yandex.practicum.interaction.category.CategoryDto;
import ru.yandex.practicum.interaction.events.EventFullDto;
import ru.yandex.practicum.interaction.events.EventShortDto;
import ru.yandex.practicum.interaction.events.EventStatistics;
import ru.yandex.practicum.interaction.events.EventWithCountConfirmedRequests;
import ru.yandex.practicum.interaction.exception.NotFoundException;
import ru.yandex.practicum.interaction.feign.clients.CategoryFeignClient;
import ru.yandex.practicum.interaction.feign.clients.RequestFeignClient;
import ru.yandex.practicum.interaction.feign.clients.UserFeignClient;
import ru.yandex.practicum.interaction.user.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventFeignServiceImpl implements EventFeignService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final StatFeignClient statFeignClient;
    private final RequestFeignClient requestFeignClient;
    private final UserFeignClient userFeignClient;
    private final CategoryFeignClient categoryFeignClient;

    @Override
    public Boolean existsByCategoryId(Long categoryId) {
        return eventRepository.existsByCategoryId(categoryId);
    }

    @Override
    public EventFullDto getEventOrThrow(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие c id " + eventId + " не найдено"));

        EventStatistics stats = getEventStatistics(List.of(event.getId()));
        UserShortDto userShortDto = userFeignClient.getUserShortDtoById(event.getInitiatorId());
        CategoryDto categoryDto = categoryFeignClient.getCategoryById(event.getCategoryId());

        return eventMapper.toEventFullDto(
                event,
                userShortDto,
                categoryDto,
                stats.confirmedRequests().getOrDefault(event.getId(), 0),
                stats.views().getOrDefault(event.getId(), 0L)
        );
    }

    @Override
    public List<EventShortDto> findAllByEventIds(List<Long> eventIds) {
        EventStatistics stats = getEventStatistics(eventIds);

        return eventRepository.findAllByIdIn(eventIds).stream()
                .map(event -> eventMapper.toEventShortDto(
                        event,
                        userFeignClient.getUserShortDtoById(event.getInitiatorId()),
                        categoryFeignClient.getCategoryById(event.getCategoryId()),
                        stats.confirmedRequests().getOrDefault(event.getId(), 0),
                        stats.views().getOrDefault(event.getId(), 0L)
                ))
                .toList();
    }

    private EventStatistics getEventStatistics(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return new EventStatistics(Map.of(), Map.of());
        }

        Map<Long, Integer> confirmedRequests = getConfirmedRequests(eventIds);
        Map<Long, Long> views = getViewsForEvents(eventIds);

        return new EventStatistics(confirmedRequests, views);
    }

    private Map<Long, Integer> getConfirmedRequests(List<Long> eventIds) {
        if (eventIds.isEmpty()) return Map.of();

        List<EventWithCountConfirmedRequests> events = requestFeignClient.findCountConfirmedRequestsByEventIds(eventIds);
        Map<Long, Integer> confirmedRequests = eventIds.stream()
                .collect(Collectors.toMap(id -> id, id -> 0));

        events.forEach(dto -> confirmedRequests.put(dto.getEventId(), dto.getCountConfirmedRequests()));

        return confirmedRequests;
    }

    private Map<Long, Long> getViewsForEvents(List<Long> eventIds) {
        if (eventIds.isEmpty()) return Map.of();

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .toList();

        LocalDateTime start = eventRepository.findFirstByOrderByCreatedOnAsc().getCreatedOn();
        LocalDateTime end = LocalDateTime.now();

        List<ViewStatsDto> stats = statFeignClient.getStats(start.toString(), end.toString(), uris, true);

        Map<Long, Long> views = eventIds.stream()
                .collect(Collectors.toMap(id -> id, id -> 0L));

        if (stats != null && !stats.isEmpty()) {
            stats.forEach(stat -> {
                Long eventId = getEventIdFromUri(stat.uri());
                if (eventId > -1L) {
                    views.put(eventId, stat.hits());
                }
            });
        }
        return views;
    }

    private Long getEventIdFromUri(String uri) {
        try {
            return Long.parseLong(uri.substring("/events".length() + 1));
        } catch (Exception e) {
            return -1L;
        }
    }
}
