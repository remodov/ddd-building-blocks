package ru.badgermock.ddd;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public abstract class DomainEvent {

    private final UUID id;
    private final OffsetDateTime createdAt;
    private final String aggregateType;
    private final String aggregateId;

    protected DomainEvent(String aggregateType, String aggregateId) {
        this(UUID.randomUUID(), OffsetDateTime.now(), aggregateType, aggregateId);
    }

    protected DomainEvent(UUID id, OffsetDateTime createdAt, String aggregateType, String aggregateId) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.aggregateType = Objects.requireNonNull(aggregateType, "aggregateType must not be null");
        this.aggregateId = Objects.requireNonNull(aggregateId, "aggregateId must not be null");
    }

    public UUID getId() {
        return id;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }
}
