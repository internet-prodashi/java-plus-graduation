package ru.yandex.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.interaction.events.EventWithCountConfirmedRequests;
import ru.yandex.practicum.interaction.request.ParticipationRequestDto;
import ru.yandex.practicum.request.model.Request;
import ru.yandex.practicum.interaction.request.enums.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    @Query("""
            SELECT NEW ru.yandex.practicum.interaction.events.EventWithCountConfirmedRequests(r.eventId, COUNT(r))
            FROM Request r
            WHERE r.eventId IN :eventIds AND r.status = ru.yandex.practicum.interaction.request.enums.RequestStatus.CONFIRMED
            GROUP BY r.eventId"""
    )
    List<EventWithCountConfirmedRequests> findCountConfirmedRequestsByEventIds(@Param("eventIds") List<Long> eventIds);

    List<Request> findByEventId(@Param("eventId") Long eventId);

    @Query("""
            SELECT r FROM Request r
            WHERE r.id IN :requestIds"""
    )
    List<Request> findAllByIdIn(@Param("requestIds") List<Long> requestIds);

    @Query("""
            SELECT NEW ru.yandex.practicum.interaction.request.ParticipationRequestDto(
                r.id,
                r.created,
                r.eventId,
                r.requesterId,
                r.status)
            FROM Request r
            WHERE r.requesterId = :userId"""
    )
    List<ParticipationRequestDto> findAllRequestsByUserId(@Param("userId") Long userId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long userId);

    int countByEventIdAndStatus(Long id, RequestStatus requestStatus);

    List<Request> findAllByEventIdAndStatus(Long eventId, RequestStatus requestStatus);
}
