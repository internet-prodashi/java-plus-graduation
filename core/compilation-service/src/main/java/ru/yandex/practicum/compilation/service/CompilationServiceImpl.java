package ru.yandex.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interaction.compilation.CompilationDto;
import ru.yandex.practicum.interaction.compilation.NewCompilationDto;
import ru.yandex.practicum.interaction.compilation.UpdateCompilationRequest;
import ru.yandex.practicum.compilation.mapper.CompilationMapper;
import ru.yandex.practicum.compilation.model.Compilation;
import ru.yandex.practicum.compilation.repository.CompilationRepository;
import ru.yandex.practicum.interaction.events.EventShortDto;
import ru.yandex.practicum.interaction.feign.clients.EventFeignClient;
import ru.yandex.practicum.interaction.exception.ConflictException;
import ru.yandex.practicum.interaction.exception.NotFoundException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventFeignClient eventFeignClient;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto request) {
        if (compilationRepository.existsByTitle(request.title()))
            throw new ConflictException("Подборка с таким названием (" + request.title() + ") уже существует");

        Compilation compilation = compilationMapper.toEntity(request);

        if (request.events() != null && !request.events().isEmpty()) {
            List<EventShortDto> events = eventFeignClient.findAllByEventIds(new ArrayList<>(request.events()));

            if (events.size() != request.events().size()) throw new NotFoundException("Не все события найдены");

            List<Long> eventIds = events.stream()
                    .map(EventShortDto::id)
                    .toList();
            compilation.setEventsIds(eventIds);

        } else compilation.setEventsIds(new ArrayList<>());
        Compilation savedCompilation = compilationRepository.save(compilation);

        log.info("Создана подборка: {}", request);
        return compilationMapper.toDto(savedCompilation, eventFeignClient.findAllByEventIds(savedCompilation.getEventsIds()));
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId))
            throw new NotFoundException("Подборка с идентификатором " + compId + " не найдена");

        log.info("Удалена подборка с id={}", compId);
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = getCompilationOrThrow(compId);

        if (request.getTitle() != null
            && !request.getTitle().equals(compilation.getTitle())
            && compilationRepository.existsByTitle(request.getTitle())) {
            throw new ConflictException("Подборка с названием \"" + request.getTitle() + "\" уже существует");
        }

        if (request.getEvents() != null) {
            if (request.getEvents().isEmpty()) {
                compilation.setEventsIds(new ArrayList<>());
            } else {
                List<EventShortDto> events = eventFeignClient.findAllByEventIds(new ArrayList<>(request.getEvents()));
                if (events.size() != request.getEvents().size()) {
                    throw new NotFoundException("Некоторые события не найдены");
                }

                List<Long> eventIds = events.stream()
                        .map(EventShortDto::id)
                        .toList();
                compilation.setEventsIds(eventIds);
            }
        }

        compilationMapper.updateCompilationFromRequest(request, compilation);
        log.info("Обновлена подборка с id={}, request={}, compilation={}", compId, request, compilation);

        return compilationMapper.toDto(compilationRepository.save(compilation),
                eventFeignClient.findAllByEventIds(compilation.getEventsIds()));
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("id").ascending()
        );

        List<Long> ids = compilationRepository.findIdsByPinned(pinned, sortedPageable);

        if (ids == null || ids.isEmpty()) {
            log.info("Список подборок пуст pinned={}, pageable={}", pinned, pageable);
            return List.of();
        }

        List<Compilation> compilationsWithEvents = compilationRepository.findAllByIdIn(ids);

        Map<Long, Compilation> byId = compilationsWithEvents.stream()
                .collect(Collectors.toMap(Compilation::getId, Function.identity()));

        List<Compilation> ordered = ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .toList();

        log.info("Получен список подборок pinned={}, pageable={}", pinned, pageable);
        return ordered.stream()
                .map(compilation ->
                        compilationMapper.toDto(
                                compilation,
                                eventFeignClient.findAllByEventIds(compilation.getEventsIds())
                        )
                )
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = getCompilationOrThrow(compId);

        log.info("Получена подборка с id={}", compId);
        return compilationMapper.toDto(compilation, eventFeignClient.findAllByEventIds(compilation.getEventsIds()));
    }

    private Compilation getCompilationOrThrow(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с идентификатором " + compId + " не найдена"));
    }
}
