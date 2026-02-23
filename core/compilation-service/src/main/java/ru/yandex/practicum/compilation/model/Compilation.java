package ru.yandex.practicum.compilation.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compilations")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false)
    private boolean pinned;

    @ElementCollection
    @CollectionTable(name = "compilation_events", joinColumns = @JoinColumn(name = "compilation_id"))
    @Column(name = "event_id")
    private List<Long> eventsIds = new ArrayList<>();

    public void setEventsIds(List<Long> eventsIds) {
        this.eventsIds = eventsIds != null ? new ArrayList<>(eventsIds) : new ArrayList<>();
    }
}
