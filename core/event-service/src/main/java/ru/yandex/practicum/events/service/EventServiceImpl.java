package ru.yandex.practicum.events.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.client.RecommendationsClient;
import ru.practicum.ewm.client.UserActionClient;
import ru.practicum.grpc.stats.action.ActionTypeProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.yandex.practicum.interaction.category.CategoryDto;
import ru.yandex.practicum.interaction.events.*;
import ru.yandex.practicum.events.mapper.EventMapper;
import ru.yandex.practicum.events.repository.EventRepository;
import ru.yandex.practicum.events.repository.SearchEventSpecifications;
import ru.yandex.practicum.interaction.events.enums.EventState;
import ru.yandex.practicum.interaction.events.enums.SortState;
import ru.yandex.practicum.interaction.events.enums.StateActionUser;
import ru.yandex.practicum.interaction.exception.BadRequestException;
import ru.yandex.practicum.interaction.exception.ConflictException;
import ru.yandex.practicum.interaction.exception.NotFoundException;
import ru.yandex.practicum.interaction.exception.ValidationException;
import ru.yandex.practicum.events.model.Event;
import ru.yandex.practicum.interaction.events.EventShortDto;
import ru.yandex.practicum.interaction.events.EventStatistics;
import ru.yandex.practicum.interaction.feign.clients.CategoryFeignClient;
import ru.yandex.practicum.interaction.feign.clients.RequestFeignClient;
import ru.yandex.practicum.interaction.feign.clients.UserFeignClient;
import ru.yandex.practicum.interaction.request.ParticipationRequestDto;
import ru.yandex.practicum.interaction.user.UserDto;
import ru.yandex.practicum.interaction.events.enums.StateActionAdmin;
import ru.yandex.practicum.interaction.user.UserShortDto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserFeignClient userFeignClient;
    private final RequestFeignClient requestFeignClient;
    private final CategoryFeignClient categoryFeignClient;
    private final RecommendationsClient recommendationsClient;
    private final UserActionClient userActionClient;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        validateDateEvent(newEventDto.eventDate(), 2);

        UserDto userDto = userFeignClient.getUserByIdOrThrow(userId);

        CategoryDto categoryDto = categoryFeignClient.getCategoryById(newEventDto.category());

        Event event = eventMapper.fromNewEvent(newEventDto, userDto, categoryDto, EventState.PENDING);
        event = eventRepository.save(event);

        UserShortDto userShortDto = userFeignClient.getUserShortDtoById(event.getInitiatorId());
        log.info("Создано событие с id={}, title={}, initiatorId={}, newEventDto={}, categoryDto={}, categoryId={}",
                event.getId(), event.getTitle(), userId, newEventDto, categoryDto, event.getCategoryId());
        return eventMapper.toEventFullDto(event, userShortDto, categoryDto, 0, 0.0);
    }

    @Override
    public List<EventShortDto> getEvents(Long userId, Pageable pageable) {
        userFeignClient.getUserByIdOrThrow(userId);
        List<Event> events = eventRepository.findAllByInitiatorIdOrderByCreatedOnDesc(userId, pageable);

        if (events.isEmpty()) return List.of();

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        EventStatistics stats = getEventStatistics(eventIds);
        Map<Long, Double> eventRatingMap = getRatingMap(eventIds);

        return events.stream()
                .map(event -> eventMapper.toEventShortDto(
                        event,
                        userFeignClient.getUserShortDtoById(event.getInitiatorId()),
                        categoryFeignClient.getCategoryById(event.getCategoryId()),
                        stats.confirmedRequests().getOrDefault(event.getId(), 0),
                        eventRatingMap.getOrDefault(event.getId(), 0.0)
                ))
                .toList();
    }

    @Override
    public EventFullDto getEvent(Long userId, Long eventId, String ip) {
        userFeignClient.getUserByIdOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Событие c id " + eventId + " не найдено у пользователя с id " + userId);
        }

        return buildFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        userFeignClient.getUserByIdOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        if (!event.getInitiatorId().equals(userId))
            throw new ValidationException("Пользователь не является инициатором события и не может его редактировать");

        if (event.getState() == EventState.PUBLISHED)
            throw new ValidationException("Изменять можно только не опубликованные события");

        validateDateEvent(request.getEventDate(), 2);

        CategoryDto categoryDto = null;
        if (request.getCategory() != null) {
            categoryDto = categoryFeignClient.getCategoryById(request.getCategory());
        }
        eventMapper.updateEventFromUserRequest(request, event, categoryDto);

        if (request.getStateAction() != null) {
            if (request.getStateAction() == StateActionUser.SEND_TO_REVIEW)
                event.pending();
            if (request.getStateAction() == StateActionUser.CANCEL_REVIEW)
                event.canceled();
        }

        Event updatedEvent = eventRepository.save(event);

        log.debug("Method: updateEvent InitiatorId={}, CategoryId={}", updatedEvent.getInitiatorId(), updatedEvent.getCategoryId());
        EventFullDto eventFullDto = buildFullDto(updatedEvent);
        log.debug("Method: EventFullDto Initiator={}, Category={}", eventFullDto.initiator(), eventFullDto.category());
        return eventFullDto;
    }

    @Override
    public Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие c id " + eventId + " не найдено"));
    }

    @Override
    public List<EventFullDto> getEventsAdmin(SearchEventAdminRequest request, Pageable pageable) {
        validateRangeStartAndEnd(request.rangeStart(), request.rangeEnd());

        Specification<Event> specification = SearchEventSpecifications.addWhereNull();
        if (request.users() != null && !request.users().isEmpty())
            specification = specification.and(SearchEventSpecifications.addWhereUsers(request.users()));
        if (request.states() != null && !request.states().isEmpty())
            specification = specification.and(SearchEventSpecifications.addWhereStates(request.states()));
        if (request.categories() != null && !request.categories().isEmpty())
            specification = specification.and(SearchEventSpecifications.addWhereCategories(request.categories()));
        if (request.rangeStart() != null)
            specification = specification.and(SearchEventSpecifications.addWhereStartsBefore(request.rangeStart()));
        if (request.rangeEnd() != null)
            specification = specification.and(SearchEventSpecifications.addWhereEndsAfter(request.rangeEnd()));
        if (request.rangeStart() == null && request.rangeEnd() == null)
            specification = specification.and(SearchEventSpecifications.addWhereStartsBefore(LocalDateTime.now()));

        Page<Long> eventIdsPage = eventRepository.findAll(specification, pageable).map(Event::getId);
        List<Long> eventIds = eventIdsPage.getContent();

        if (eventIds.isEmpty()) return List.of();
        List<Event> events = eventRepository.findAllByIdIn(eventIds);

        if (events.isEmpty()) return List.of();

        List<Long> searchEventIds = events.stream()
                .map(Event::getId)
                .toList();

        EventStatistics stats = getEventStatistics(searchEventIds);
        Map<Long, Double> eventRatingMap = getRatingMap(searchEventIds);

        return events.stream()
                .map(event -> eventMapper.toEventFullDto(
                        event,
                        userFeignClient.getUserShortDtoById(event.getInitiatorId()),
                        categoryFeignClient.getCategoryById(event.getCategoryId()),
                        stats.confirmedRequests().getOrDefault(event.getId(), 0),
                        eventRatingMap.getOrDefault(event.getId(), 0.0)
                ))
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие c id " + eventId + " не найдено"));

        if (request.stateAction() != null) {
            StateActionAdmin stateActionAdmin = request.stateAction();
            EventState currentState = event.getState();

            if (stateActionAdmin == StateActionAdmin.PUBLISH_EVENT) {
                if (currentState != EventState.PENDING)
                    throw new ConflictException("Событие можно опубликовать только если оно в состоянии ожидания публикации");
                validateDateEvent(event.getEventDate(), 1);
                event.publish();
            }
            if (stateActionAdmin == StateActionAdmin.REJECT_EVENT) {
                if (currentState == EventState.PUBLISHED)
                    throw new ConflictException("Событие можно отклонить пока оно не опубликовано");
                event.canceled();
            }
        }

        CategoryDto categoryDto = null;
        if (request.category() != null) {
            categoryDto = categoryFeignClient.getCategoryById(request.category());
        }

        eventMapper.updateEventFromAdminRequest(request, event, categoryDto);
        Event updatedEvent = eventRepository.save(event);

        log.info("Обновлено событие с id={}, title={}, initiatorId={}, request={}, categoryDto={}, categoryId={}",
                updatedEvent.getId(), updatedEvent.getTitle(), updatedEvent.getInitiatorId(), request, categoryDto, event.getCategoryId());

        return buildFullDto(updatedEvent);
    }

    @Override
    public List<EventShortDto> getEventsPublic(SearchEventPublicRequest request, Pageable pageable, String ip) {
        validateRangeStartAndEnd(request.rangeStart(), request.rangeEnd());

        Specification<Event> specification = SearchEventSpecifications.addWhereNull();
        if (request.text() != null && !request.text().trim().isEmpty())
            specification = specification.and(SearchEventSpecifications.addLikeText(request.text()));
        if (request.categories() != null && !request.categories().isEmpty())
            specification = specification.and(SearchEventSpecifications.addWhereCategories(request.categories()));
        if (request.paid() != null)
            specification = specification.and(SearchEventSpecifications.isPaid(request.paid()));
        LocalDateTime rangeStart = (request.rangeStart() == null && request.rangeEnd() == null) ?
                LocalDateTime.now() : request.rangeStart();
        if (rangeStart != null)
            specification = specification.and(SearchEventSpecifications.addWhereStartsBefore(rangeStart));
        if (request.rangeEnd() != null)
            specification = specification.and(SearchEventSpecifications.addWhereEndsAfter(request.rangeEnd()));
        if (request.onlyAvailable())
            specification = specification.and(SearchEventSpecifications.addWhereAvailableSlots());

        List<Event> events = eventRepository.findAll(specification, pageable).getContent();

        if (events.isEmpty()) return List.of();

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        EventStatistics stats = getEventStatistics(eventIds);
        Map<Long, Double> eventRatingMap = getRatingMap(eventIds);

        List<EventShortDto> result = events.stream()
                .map(event -> eventMapper.toEventShortDto(
                        event,
                        userFeignClient.getUserShortDtoById(event.getInitiatorId()),
                        categoryFeignClient.getCategoryById(event.getCategoryId()),
                        stats.confirmedRequests().getOrDefault(event.getId(), 0),
                        eventRatingMap.getOrDefault(event.getId(), 0.0)
                ))
                .toList();

        if (SortState.VIEWS.equals(request.sort())) {
            return result.stream()
                    .sorted(Comparator.comparing(EventShortDto::rating).reversed())
                    .toList();
        } else if (SortState.EVENT_DATE.equals(request.sort())) {
            return result.stream()
                    .sorted(Comparator.comparing(EventShortDto::eventDate))
                    .toList();
        }

        return result;
    }

    @Override
    public EventFullDto getEventByIdPublic(Long eventId, String ip) {
        Event event = eventRepository.findById(eventId)
                .filter(ev -> ev.getState() == EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие c id " + eventId + " не найдено"));

        return buildFullDto(event);
    }

    @Override
    public List<EventShortDto> getRecommendation(Long userId, int maxResult) {
        Stream<RecommendedEventProto> recommendedEvent = recommendationsClient.getRecommendationsForUser(userId, maxResult);

        List<Long> eventIds = recommendedEvent.map(RecommendedEventProto::getEventId).toList();
        List<Event> events = eventRepository.findAllById(eventIds);
        EventStatistics stats = getEventStatistics(eventIds);
        Map<Long, Double> eventRatingMap = getRatingMap(eventIds);

        return events.stream()
                .map(event -> eventMapper.toEventShortDto(
                        event,
                        userFeignClient.getUserShortDtoById(event.getInitiatorId()),
                        categoryFeignClient.getCategoryById(event.getCategoryId()),
                        stats.confirmedRequests().getOrDefault(event.getId(), 0),
                        eventRatingMap.getOrDefault(event.getId(), 0.0)
                )).toList();
    }

    @Override
    public void addLikeToEvent(Long eventId, Long userId) {
        userFeignClient.getUserByIdOrThrow(userId);

        getEventOrThrow(eventId);

        ParticipationRequestDto requestDto = requestFeignClient.getUserRequest(userId, eventId);
        if (!requestDto.status().toString().equals("CONFIRMED"))
            throw new ValidationException("You cannot like an event without confirmed participation");

        userActionClient.collectUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE, Instant.now());
    }

    private EventFullDto buildFullDto(Event event) {
        Map<Long, Integer> confirmedRequests = getConfirmedRequests(List.of(event.getId()));
        Map<Long, Double> eventRatingMap = getRatingMap(List.of(event.getId()));
        UserShortDto userShortDto = userFeignClient.getUserShortDtoById(event.getInitiatorId());
        CategoryDto categoryDto = categoryFeignClient.getCategoryById(event.getCategoryId());

        return eventMapper.toEventFullDto(
                event,
                userShortDto,
                categoryDto,
                confirmedRequests.getOrDefault(event.getId(), 0),
                eventRatingMap.getOrDefault(event.getId(), 0.0)
        );
    }

    private void validateRangeStartAndEnd(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd))
            throw new BadRequestException("Дата начала не может быть позже даты окончания");
    }

    private void validateDateEvent(LocalDateTime eventDate, long minHoursBeforeStartEvent) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(minHoursBeforeStartEvent)))
            throw new ValidationException("Дата начала события не может быть ранее чем через " + minHoursBeforeStartEvent + " часа(ов)");
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

    private EventStatistics getEventStatistics(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return new EventStatistics(Map.of());
        }

        Map<Long, Integer> confirmedRequests = getConfirmedRequests(eventIds);

        return new EventStatistics(confirmedRequests);
    }

    public Map<Long, Double> getRatingMap(List<Long> eventIds) {
        Stream<RecommendedEventProto> eventRating = recommendationsClient.getInteractionsCount(eventIds);

        return eventRating
                .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));
    }
}