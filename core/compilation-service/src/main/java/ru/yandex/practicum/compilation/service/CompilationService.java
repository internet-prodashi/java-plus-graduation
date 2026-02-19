package ru.yandex.practicum.compilation.service;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.interaction.compilation.CompilationDto;
import ru.yandex.practicum.interaction.compilation.NewCompilationDto;
import ru.yandex.practicum.interaction.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto createCompilation(NewCompilationDto request);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request);

    List<CompilationDto> getCompilations(Boolean pinned, Pageable pageable);

    CompilationDto getCompilationById(Long compId);
}