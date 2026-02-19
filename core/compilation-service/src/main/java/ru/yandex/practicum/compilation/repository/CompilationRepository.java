package ru.yandex.practicum.compilation.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.compilation.model.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    boolean existsByTitle(String title);

    @Query("""
            SELECT c.id FROM Compilation c
            WHERE (:pinned IS NULL OR c.pinned = :pinned)
            ORDER BY c.id ASC""")
    List<Long> findIdsByPinned(@Param("pinned") Boolean pinned, Pageable pageable);

    List<Compilation> findAllByIdIn(@Param("ids") List<Long> ids);
}
