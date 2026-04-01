package ru.badgermock.ddd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class AggregateRoot<ID> extends Entity<ID> {

    private final List<DomainEvent> events = new ArrayList<>();

    protected void registerEvent(DomainEvent event) {
        Objects.requireNonNull(event, "Domain event must not be null");
        events.add(event);
    }

    public List<DomainEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public void clearDomainEvents() {
        events.clear();
    }
}
