package ru.yandex.practicum.comments.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.comments.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByIdAndUserIdAndEventId(@Param("id") Long id, @Param("userId") Long userId, @Param("eventId") Long eventId);

    Optional<Comment> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    List<Comment> findByEventId(@Param("eventId") Long eventId, Pageable pageable);

    List<Comment> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
