package ru.badgermock.ddd;

public abstract class Entity<ID> {

    public abstract ID getId();
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> other = (Entity<?>) o;
        ID id = getId();
        return id != null && id.equals(other.getId());
    }

    @Override
    public final int hashCode() {
        ID id = getId();
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
