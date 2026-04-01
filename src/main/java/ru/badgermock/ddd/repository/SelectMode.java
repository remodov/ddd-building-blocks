package ru.badgermock.ddd.repository;

public enum SelectMode {

    NO_LOCK,
    FOR_UPDATE,
    FOR_UPDATE_SKIP_LOCKED
}
