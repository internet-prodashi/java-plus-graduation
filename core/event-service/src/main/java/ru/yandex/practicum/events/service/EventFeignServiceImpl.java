package ru.yandex.practicum.events.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.client.RecommendationsClient;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventFeignServiceImpl implements EventFeignService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final RequestFeignClient requestFeignClient;
    private final UserFeignClient userFeignClient;
    private final CategoryFeignClient categoryFeignClient;
    private final RecommendationsClient recommendationsClient;

    @Override
    public Boolean existsByCategoryId(Long categoryId) {
        return eventRepository.existsByCategoryId(categoryId);
    }

    @Override
    public EventFullDto getEventOrThrow(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие c id " + eventId + " не найдено"));

        EventStatistics stats = getEventStatistics(List.of(event.getId()));
        Map<Long, Double> eventRatingMap = getRatingMap(List.of(event.getId()));
        UserShortDto userShortDto = userFeignClient.getUserShortDtoById(event.getInitiatorId());
        CategoryDto categoryDto = categoryFeignClient.getCategoryById(event.getCategoryId());

        return eventMapper.toEventFullDto(
                event,
                userShortDto,
                categoryDto,
                stats.confirmedRequests().getOrDefault(event.getId(), 0),
                eventRatingMap.getOrDefault(event.getId(), 0.0)
                //stats.views().getOrDefault(event.getId(), 0L)
        );
    }

    @Override
    public List<EventShortDto> findAllByEventIds(List<Long> eventIds) {
        EventStatistics stats = getEventStatistics(eventIds);
        Map<Long, Double> eventRatingMap = getRatingMap(eventIds);

        return eventRepository.findAllByIdIn(eventIds).stream()
                .map(event -> eventMapper.toEventShortDto(
                        event,
                        userFeignClient.getUserShortDtoById(event.getInitiatorId()),
                        categoryFeignClient.getCategoryById(event.getCategoryId()),
                        stats.confirmedRequests().getOrDefault(event.getId(), 0),
                        eventRatingMap.getOrDefault(event.getId(), 0.0)
                        //stats.views().getOrDefault(event.getId(), 0L)
                ))
                .toList();
    }

    private EventStatistics getEventStatistics(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return new EventStatistics(Map.of());
        }

        Map<Long, Integer> confirmedRequests = getConfirmedRequests(eventIds);

        return new EventStatistics(confirmedRequests);
    }

    private Map<Long, Integer> getConfirmedRequests(List<Long> eventIds) {
        if (eventIds.isEmpty()) return Map.of();

        List<EventWithCountConfirmedRequests> events = requestFeignClient.findCountConfirmedRequestsByEventIds(eventIds);
        Map<Long, Integer> confirmedRequests = eventIds.stream()
                .collect(Collectors.toMap(id -> id, id -> 0));

        events.forEach(dto -> confirmedRequests.put(dto.getEventId(), dto.getCountConfirmedRequests()));

        return confirmedRequests;
    }

    private Long getEventIdFromUri(String uri) {
        try {
            return Long.parseLong(uri.substring("/events".length() + 1));
        } catch (Exception e) {
            return -1L;
        }
    }

    public Map<Long, Double> getRatingMap(List<Long> eventIds) {
        Stream<RecommendedEventProto> eventRating = recommendationsClient.getInteractionsCount(eventIds);

        return eventRating
                .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));
    }
}
