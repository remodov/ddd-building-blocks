# DDD Building Blocks

Java-библиотека с базовыми строительными блоками для Domain-Driven Design.

## Подключение

### Gradle

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/remodov/ddd-building-blocks")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("ru.badgermock:ddd-building-blocks:1.0.0")
}
```

### Maven

```xml
<repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/remodov/ddd-building-blocks</url>
</repository>

<dependency>
    <groupId>ru.badgermock</groupId>
    <artifactId>ddd-building-blocks</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Состав

| Класс / Интерфейс | Тип | Описание |
|---|---|---|
| `Entity<ID>` | abstract class | Базовая сущность с идентичностью по ID (`equals`/`hashCode`) |
| `AggregateRoot<ID>` | abstract class | Агрегат с регистрацией доменных событий |
| `ValueObject` | interface | Маркер для объектов-значений |
| `DomainEvent` | abstract class | Доменное событие с `id`, `createdAt`, `aggregateType`, `aggregateId` |
| `DomainEventPublisher` | interface | Публикация доменных событий |
| `DomainEventHandler<E>` | interface | Обработчик доменного события (functional interface) |
| `AggregateRepository<T, ID>` | interface | Репозиторий агрегата: `findById`, `save`, `delete` |
| `Specification<T>` | abstract class | Спецификация с комбинаторами `and`, `or`, `not` |

## Пример использования

```java
public class Order extends AggregateRoot<UUID> {

    private final UUID id;
    private OrderStatus status;

    public Order(UUID id) {
        this.id = id;
        this.status = OrderStatus.CREATED;
        registerEvent(new OrderCreatedEvent(id));
    }

    @Override
    public UUID getId() {
        return id;
    }
}
```

## Лицензия

MIT
