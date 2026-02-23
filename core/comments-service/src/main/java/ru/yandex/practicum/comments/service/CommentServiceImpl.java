package ru.yandex.practicum.comments.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interaction.comments.CommentDto;
import ru.yandex.practicum.interaction.comments.NewCommentDto;
import ru.yandex.practicum.interaction.comments.UpdateCommentDto;
import ru.yandex.practicum.comments.mapper.CommentMapper;
import ru.yandex.practicum.comments.model.Comment;
import ru.yandex.practicum.comments.repository.CommentRepository;
import ru.yandex.practicum.interaction.events.EventFullDto;
import ru.yandex.practicum.interaction.events.enums.EventState;
import ru.yandex.practicum.interaction.exception.ConflictException;
import ru.yandex.practicum.interaction.exception.NotFoundException;
import ru.yandex.practicum.interaction.user.UserDto;
import ru.yandex.practicum.interaction.feign.clients.UserFeignClient;
import ru.yandex.practicum.interaction.feign.clients.EventFeignClient;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserFeignClient userFeignClient;
    private final EventFeignClient eventFeignClient;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        UserDto user = userFeignClient.getUserByIdOrThrow(userId);
        EventFullDto event = eventFeignClient.getEventOrThrow(eventId);

        if (EventState.PUBLISHED != event.state())
            throw new ConflictException("Нельзя комментировать неопубликованное событие");

        Comment comment = commentMapper.toComment(newCommentDto, user, event);

        log.info("Создан новый комментарий: authorId={}, eventId={}, newCommentDto={}", userId, eventId, newCommentDto);
        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long eventId, Long commentId, UpdateCommentDto updateCommentDto) {
        Comment comment = commentRepository.findByIdAndUserIdAndEventId(commentId, userId, eventId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id=" + commentId
                                                         + " от пользователя с id=" + userId
                                                         + " к событию с id=" + eventId + " не найден"));

        commentMapper.updateCommentFromDto(updateCommentDto, comment);

        log.info("Комментарий обновлен: commentId={}, userId={}, eventId={}, updateCommentDto={}",
                commentId, userId, eventId, updateCommentDto);
        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        commentRepository.findByIdAndUserId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id=" + commentId
                                                         + " от пользователя с id=" + userId + " не найден"));

        log.info("Комментарий удален: commentId={}, userId={}", commentId, userId);
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public void deleteCommentAdmin(Long commentId) {
        commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id=" + commentId + " не найден"));

        log.info("Комментарий удален администратором: commentId={}", commentId);
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> getCommentsForEvent(Long eventId, Pageable pageable) {
        eventFeignClient.getEventOrThrow(eventId);

        log.info("Получен список комментариев для события: eventId={}", eventId);
        return commentRepository.findByEventId(eventId, pageable)
                .stream()
                .map(commentMapper::toDto)
                .toList();
    }

    @Override
    public List<CommentDto> getCommentsForUser(Long userId, Pageable pageable) {
        userFeignClient.getUserByIdOrThrow(userId);

        log.info("Получен список комментариев для пользователя: userId={}", userId);
        return commentRepository.findByUserId(userId, pageable)
                .stream()
                .map(commentMapper::toDto)
                .toList();
    }
}