package ru.yandex.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interaction.exception.NotFoundException;
import ru.yandex.practicum.interaction.user.UserDto;
import ru.yandex.practicum.interaction.user.UserShortDto;
import ru.yandex.practicum.user.mapper.UserMapper;
import ru.yandex.practicum.user.repository.UserRepository;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserFeignServiceImpl implements UserFeignService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto getUserByIdOrThrow(Long userId) {
        return userMapper.toDto(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден")));
    }

    @Override
    public UserShortDto getUserShortDtoById(Long userId) {
        return userMapper.toShortDto(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден")));
    }
}
