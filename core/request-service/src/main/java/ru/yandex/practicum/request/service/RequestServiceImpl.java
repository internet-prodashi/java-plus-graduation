package ru.yandex.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interaction.events.EventFullDto;
import ru.yandex.practicum.interaction.events.enums.EventState;
import ru.yandex.practicum.interaction.exception.ConflictException;
import ru.yandex.practicum.interaction.exception.NotFoundException;
import ru.yandex.practicum.interaction.exception.ValidationException;
import ru.yandex.practicum.interaction.feign.clients.EventFeignClient;
import ru.yandex.practicum.interaction.feign.clients.UserFeignClient;
import ru.yandex.practicum.interaction.request.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.interaction.request.EventRequestStatusUpdateResult;
import ru.yandex.practicum.interaction.request.ParticipationRequestDto;
import ru.yandex.practicum.interaction.user.UserDto;
import ru.yandex.practicum.request.mapper.RequestMapper;
import ru.yandex.practicum.request.model.Request;
import ru.yandex.practicum.interaction.request.enums.RequestStatus;
import ru.yandex.practicum.request.repository.RequestRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final EventFeignClient eventFeignClient;
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        UserDto user = userFeignClient.getUserByIdOrThrow(userId);
        EventFullDto event = eventFeignClient.getEventOrThrow(eventId);

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId))
            throw new ConflictException("Нельзя добавить повторный запрос");

        if (event.initiator().id().equals(userId))
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");

        if (event.state() != EventState.PUBLISHED)
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");

        if (!hasSlots(event)) {
            throw new ConflictException("Достигнут лимит по количеству участников события с id=" + eventId);
        }

        Request request = requestMapper.toEntity(event, user, RequestStatus.PENDING);

        if (!event.requestModeration() || event.participantLimit() == 0) {
            log.debug("Модерация заявок на участие в событии с id={} не требуется", eventId);
            request.confirmed();
        }

        request = requestRepository.save(request);

        log.info("Добавление нового запроса на участие в событии с id={} от пользователя с id={}", eventId, userId);
        return requestMapper.toDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByUserId(Long userId) {
        userFeignClient.getUserByIdOrThrow(userId);

        log.info("Получение информации о заявках на участие пользователя с id={}", userId);
        return requestRepository.findAllRequestsByUserId(userId);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        userFeignClient.getUserByIdOrThrow(userId);
        Request request = getRequestOrThrow(requestId);

        if (!request.getRequesterId().equals(userId)) {
            throw new ConflictException("Запрос с id=" + requestId + " не принадлежит пользователю с id=" + userId);
        }

        request.canceled();
        request = requestRepository.save(request);
        log.info("Отмена запроса на участие с id={} пользователя с id={}", requestId, userId);
        return requestMapper.toDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByEvent(Long userId, Long eventId) {
        EventFullDto event = eventFeignClient.getEventOrThrow(eventId);

        if (!event.initiator().id().equals(userId))
            throw new ValidationException("Пользователь с id=" + userId + " не является создателем события");

        log.info("Получение информации о запросах на участие в событии с id={}", eventId);
        return requestRepository.findByEventId(eventId).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        EventFullDto event = eventFeignClient.getEventOrThrow(eventId);

        if (!event.initiator().id().equals(userId))
            throw new ValidationException("Пользователь с id=" + userId + " не является создателем события");

        if (!event.requestModeration() || event.participantLimit() == 0)
            throw new ValidationException("Для данного события подтверждение заявок не требуется");

        RequestStatus newStatus = request.status();
        if (newStatus == RequestStatus.PENDING)
            throw new ValidationException("Устанавливать можно только статусы CONFIRMED или REJECTED");

        List<Request> requestsForUpdate = requestRepository.findAllByIdIn(request.requestIds());

        validateAllRequestsExist(request.requestIds(), requestsForUpdate);
        validateRequestsState(requestsForUpdate, eventId);

        int currentConfirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        int availableSlots = event.participantLimit() - currentConfirmedCount;

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        if (newStatus == RequestStatus.CONFIRMED) {
            if (availableSlots <= 0) throw new ConflictException("Свободных мест больше нет");

            int confirmedCount = 0;
            for (Request req : requestsForUpdate) {
                if (confirmedCount < availableSlots) {
                    req.confirmed();
                    confirmedRequests.add(requestMapper.toDto(req));
                    confirmedCount++;
                } else {
                    req.rejected();
                    rejectedRequests.add(requestMapper.toDto(req));
                }
            }

            if (currentConfirmedCount + confirmedCount >= event.participantLimit()) {
                List<Request> pendingRequests = requestRepository
                        .findAllByEventIdAndStatus(eventId, RequestStatus.PENDING);

                for (Request pendingReq : pendingRequests) {
                    pendingReq.rejected();
                    rejectedRequests.add(requestMapper.toDto(pendingReq));
                }

                if (!pendingRequests.isEmpty()) {
                    requestRepository.saveAll(pendingRequests);
                    log.info("Автоматически отклонено {} заявок из-за исчерпания лимита на событие с id={}",
                            pendingRequests.size(), eventId);
                }
            }
        } else {
            for (Request req : requestsForUpdate) {
                req.rejected();
                rejectedRequests.add(requestMapper.toDto(req));
            }
        }

        requestRepository.saveAll(requestsForUpdate);

        log.info("Обновление статусов заявок на участие в событии с id={}: подтверждено={}, отклонено={}",
                eventId, confirmedRequests.size(), rejectedRequests.size());

        return requestMapper.toEventRequestStatusUpdateResultDto(confirmedRequests, rejectedRequests);
    }

    private Request getRequestOrThrow(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден"));
    }

    private boolean hasSlots(EventFullDto event) {
        Integer limit = event.participantLimit();
        if (limit == null || limit == 0) return true;
        long confirmed = requestRepository.countByEventIdAndStatus(event.id(), RequestStatus.CONFIRMED);
        return confirmed < limit;
    }

    private void validateAllRequestsExist(List<Long> requestedIds, List<Request> foundRequests) {
        List<Long> foundIds = foundRequests.stream()
                .map(Request::getId)
                .toList();

        List<Long> missingIds = requestedIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new NotFoundException("Запрос(ы) с id=" + missingIds + " не найден(ы)");
        }
    }

    private void validateRequestsState(List<Request> requests, Long eventId) {
        for (Request req : requests) {
            if (req.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Можно изменять только запросы в статусе PENDING");
            }

            if (!req.getEventId().equals(eventId)) {
                throw new ConflictException("Запрос с id=" + req.getId() + " не относится к событию с id=" + eventId);
            }
        }
    }
}