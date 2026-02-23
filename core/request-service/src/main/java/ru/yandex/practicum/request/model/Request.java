package ru.yandex.practicum.request.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import ru.yandex.practicum.interaction.request.enums.RequestStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @JoinColumn(name = "event_id", nullable = false)
    private Long eventId;

    @JoinColumn(name = "requester_id", nullable = false)
    private Long requesterId;

    @CreationTimestamp
    private LocalDateTime created;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    public void confirmed() {
        this.status = RequestStatus.CONFIRMED;
    }

    public void rejected() {
        this.status = RequestStatus.REJECTED;
    }

    public void canceled() {
        this.status = RequestStatus.CANCELED;
    }
}
