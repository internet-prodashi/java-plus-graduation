package ru.yandex.practicum.comments.service;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.interaction.comments.CommentDto;
import ru.yandex.practicum.interaction.comments.NewCommentDto;
import ru.yandex.practicum.interaction.comments.UpdateCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto updateComment(Long userId, Long eventId, Long commentId, UpdateCommentDto updateCommentDto);

    void deleteComment(Long userId, Long commentId);

    void deleteCommentAdmin(Long commentId);

    List<CommentDto> getCommentsForEvent(Long eventId, Pageable pageable);

    List<CommentDto> getCommentsForUser(Long userId, Pageable pageable);
}