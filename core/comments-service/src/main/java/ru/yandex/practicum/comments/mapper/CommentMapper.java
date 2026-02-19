package ru.yandex.practicum.comments.mapper;

import org.mapstruct.*;
import ru.yandex.practicum.interaction.comments.CommentDto;
import ru.yandex.practicum.interaction.comments.NewCommentDto;
import ru.yandex.practicum.interaction.comments.UpdateCommentDto;
import ru.yandex.practicum.comments.model.Comment;
import ru.yandex.practicum.interaction.events.EventFullDto;
import ru.yandex.practicum.interaction.user.UserDto;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    Comment toComment(NewCommentDto newCommentDto, UserDto user, EventFullDto event);

    CommentDto toDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    void updateCommentFromDto(UpdateCommentDto dto, @MappingTarget Comment comment);
}
