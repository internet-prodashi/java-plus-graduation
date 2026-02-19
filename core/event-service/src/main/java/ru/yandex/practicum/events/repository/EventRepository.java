package ru.yandex.practicum.events.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.events.model.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    List<Event> findAllByInitiatorIdOrderByCreatedOnDesc(Long initiatorId, Pageable pageable);

    Event findFirstByOrderByCreatedOnAsc();

    List<Event> findAllByIdIn(@Param("eventIds") List<Long> eventIds);

    boolean existsByCategoryId(@Param("categoryId") Long categoryId);
}
