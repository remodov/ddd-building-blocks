package ru.badgermock.ddd;

import ru.badgermock.ddd.repository.SelectMode;

import java.util.Optional;

public interface Repository<T extends AggregateRoot<ID>, ID> {

    Optional<T> findById(ID id, SelectMode mode);

    T save(T aggregate);
}
