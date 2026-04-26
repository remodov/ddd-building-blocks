package ru.badgermock.ddd;

import java.util.List;

public interface DomainEventPublisher {

    void publish(DomainEvent event);

    default void publishAll(List<? extends DomainEvent> events) {
        events.forEach(this::publish);
    }
}
