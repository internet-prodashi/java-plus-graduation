package ru.yandex.practicum.interaction.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventWithCountConfirmedRequests {
    private Long eventId;
    private int countConfirmedRequests;

    public EventWithCountConfirmedRequests(Long eventId, Long countConfirmedRequestsLong) {
        this.eventId = eventId;
        this.countConfirmedRequests = (countConfirmedRequestsLong == null) ? 0 : countConfirmedRequestsLong.intValue();
    }
}
