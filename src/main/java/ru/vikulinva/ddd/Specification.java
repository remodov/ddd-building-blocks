package ru.badgermock.ddd;

import java.util.Objects;

public abstract class Specification<T> {

    public abstract boolean isSatisfiedBy(T candidate);

    public Specification<T> and(Specification<T> other) {
        Objects.requireNonNull(other, "Specification must not be null");
        Specification<T> self = this;
        return new Specification<>() {
            @Override
            public boolean isSatisfiedBy(T candidate) {
                return self.isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate);
            }
        };
    }

    public Specification<T> or(Specification<T> other) {
        Objects.requireNonNull(other, "Specification must not be null");
        Specification<T> self = this;
        return new Specification<>() {
            @Override
            public boolean isSatisfiedBy(T candidate) {
                return self.isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate);
            }
        };
    }

    public Specification<T> not() {
        Specification<T> self = this;
        return new Specification<>() {
            @Override
            public boolean isSatisfiedBy(T candidate) {
                return !self.isSatisfiedBy(candidate);
            }
        };
    }
}
