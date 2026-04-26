package ru.badgermock.ddd;

@FunctionalInterface
public interface DomainEventHandler<E extends DomainEvent> {

    void handle(E event);
}
