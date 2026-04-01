# DDD Building Blocks

Библиотека базовых абстракций Domain-Driven Design для Java-проектов.

Zero dependencies. Чистая Java.

## Подключение

```kotlin
dependencies {
    implementation("ru.badgermock:ddd-building-blocks:1.0.0")
}
```

## Абстракции

- [Entity](#entity) — сущность с идентичностью
- [ValueObject](#valueobject) — объект-значение
- [AggregateRoot](#aggregateroot) — корень агрегата
- [DomainEvent](#domainevent) — доменное событие
- [DomainEventPublisher](#domaineventpublisher) — публикация событий
- [DomainEventHandler](#domaineventhandler) — обработка событий
- [AggregateRepository](#aggregaterepository) — репозиторий агрегатов
- [Specification](#specification) — спецификация бизнес-правил

---

### Entity

Сущность идентифицируется по ID, а не по атрибутам. `equals`/`hashCode` реализованы в базовом классе и финализированы — переопределить нельзя.

```java
public class Product extends Entity<ProductId> {

    private final ProductId id;
    private String name;
    private Money price;

    public Product(ProductId id, String name, Money price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    @Override
    public ProductId getId() {
        return id;
    }

    public void changePrice(Money newPrice) {
        this.price = newPrice;
    }
}
```

Два `Product` с одинаковым `ProductId` считаются одной и той же сущностью:

```java
Product a = new Product(productId, "Молоко", money(99));
Product b = new Product(productId, "Молоко", money(149));
a.equals(b); // true — тот же ID
```

---

### ValueObject

Объект-значение определяется своими атрибутами. Иммутабелен. Реализуется через Java `record` — `equals`, `hashCode` и `toString` генерируются автоматически.

```java
public record Money(BigDecimal amount, String currency) implements ValueObject {

    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
    }

    public Money add(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add different currencies");
        }
        return new Money(amount.add(other.amount), currency);
    }
}
```

---

### AggregateRoot

Корень агрегата — граница консистентности. Все изменения вложенных Entity и ValueObject проходят через корень. Поддерживает доменные события и версионирование для оптимистичной блокировки.

```java
public class Order extends AggregateRoot<OrderId> {

    private final OrderId id;
    private final List<OrderLine> lines = new ArrayList<>();
    private OrderStatus status;

    public Order(OrderId id) {
        this.id = id;
        this.status = OrderStatus.DRAFT;
    }

    @Override
    public OrderId getId() {
        return id;
    }

    public void place() {
        if (lines.isEmpty()) {
            throw new IllegalStateException("Cannot place an empty order");
        }
        this.status = OrderStatus.PLACED;
        registerEvent(new OrderPlacedEvent(id, calculateTotal()));
    }

    public void addLine(ProductId productId, int quantity, Money price) {
        lines.add(new OrderLine(productId, quantity, price));
    }

    private Money calculateTotal() {
        // ...
    }
}
```

---

### DomainEvent

Доменное событие — факт, который произошёл в домене. Несёт информацию об агрегате-источнике. Два конструктора: для создания новых событий и для восстановления из хранилища.

```java
public class OrderPlacedEvent extends DomainEvent {

    private final Money totalAmount;

    // Новое событие — UUID и timestamp генерируются автоматически
    public OrderPlacedEvent(OrderId orderId, Money totalAmount) {
        super("Order", orderId.toString());
        this.totalAmount = totalAmount;
    }

    // Восстановление из хранилища / message bus
    public OrderPlacedEvent(UUID eventId, Instant occurredAt,
                            String aggregateId, Money totalAmount) {
        super(eventId, occurredAt, "Order", aggregateId);
        this.totalAmount = totalAmount;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }
}
```

---

### DomainEventPublisher

Интерфейс публикации событий. Домен определяет контракт, инфраструктура реализует.

```java
// Реализация через Spring Events
public class SpringEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
```

Типичное использование в репозитории (jOOQ):

```java
public class JooqOrderRepository implements OrderRepository {

    private final DSLContext dsl;
    private final DomainEventPublisher eventPublisher;

    @Override
    public Order save(Order aggregate) {
        dsl.update(ORDERS)
                .set(ORDERS.STATUS, aggregate.getStatus().name())
                .set(ORDERS.TOTAL_AMOUNT, aggregate.getTotalAmount())
                .set(ORDERS.UPDATED_AT, LocalDateTime.now())
                .where(ORDERS.ID.eq(aggregate.getId().getValue()))
                .execute();

        eventPublisher.publishAll(aggregate.getEvents());
        aggregate.clearDomainEvents();
        return aggregate;
    }
}
```

---

### DomainEventHandler

Типизированный обработчик доменных событий. Functional interface — можно использовать как лямбду.

```java
public class SendConfirmationOnOrderPlaced
        implements DomainEventHandler<OrderPlacedEvent> {

    private final NotificationService notifications;

    public SendConfirmationOnOrderPlaced(NotificationService notifications) {
        this.notifications = notifications;
    }

    @Override
    public void handle(OrderPlacedEvent event) {
        notifications.sendOrderConfirmation(
                event.getAggregateId(),
                event.getTotalAmount()
        );
    }
}
```

---

### AggregateRepository

Интерфейс персистенции агрегатов (write-side). Работает только с `AggregateRoot`. Блокировки — ответственность конкретной реализации.

Для read-side (запросы, DTO, проекции) общая абстракция не предусмотрена — каждый сервис определяет свои query-интерфейсы под свои нужды напрямую через jOOQ/SQL. Это стандартный подход CQRS: write-side формализован, read-side свободен.

```java
// Доменный интерфейс
public interface OrderRepository extends AggregateRepository<Order, OrderId> {

    List<Order> findByStatus(OrderStatus status);

    // Если нужна блокировка — описываем намерение, не механизм
    Optional<Order> findByIdForModification(OrderId id);
}

// Инфраструктурная реализация на jOOQ
public class JooqOrderRepository implements OrderRepository {

    private final DSLContext dsl;
    private final DomainEventPublisher eventPublisher;

    @Override
    public Optional<Order> findById(OrderId id) {
        return dsl.selectFrom(ORDERS)
                .where(ORDERS.ID.eq(id.getValue()))
                .fetchOptional(this::toOrder);
    }

    @Override
    public Optional<Order> findByIdForModification(OrderId id) {
        return dsl.selectFrom(ORDERS)
                .where(ORDERS.ID.eq(id.getValue()))
                .forUpdate()
                .fetchOptional(this::toOrder);
    }

    @Override
    public Order save(Order aggregate) {
        dsl.update(ORDERS)
                .set(ORDERS.STATUS, aggregate.getStatus().name())
                .set(ORDERS.TOTAL_AMOUNT, aggregate.getTotalAmount())
                .set(ORDERS.UPDATED_AT, LocalDateTime.now())
                .where(ORDERS.ID.eq(aggregate.getId().getValue()))
                .execute();

        eventPublisher.publishAll(aggregate.getEvents());
        aggregate.clearDomainEvents();
        return aggregate;
    }

    @Override
    public void delete(Order aggregate) {
        dsl.deleteFrom(ORDERS)
                .where(ORDERS.ID.eq(aggregate.getId().getValue()))
                .execute();
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return dsl.selectFrom(ORDERS)
                .where(ORDERS.STATUS.eq(status.name()))
                .fetch(this::toOrder);
    }

    private Order toOrder(OrdersRecord record) {
        return new Order(new OrderId(record.getId()));
    }
}
```

---

### Specification

Инкапсуляция бизнес-правил. Композиция через `and`, `or`, `not`.

```java
public class OrderIsOverdue extends Specification<Order> {

    @Override
    public boolean isSatisfiedBy(Order order) {
        return order.getDueDate().isBefore(Instant.now());
    }
}

public class OrderIsUnpaid extends Specification<Order> {

    @Override
    public boolean isSatisfiedBy(Order order) {
        return !order.isPaid();
    }
}
```

Использование:

```java
Specification<Order> needsAttention = new OrderIsOverdue()
        .and(new OrderIsUnpaid());

Specification<Order> canBeArchived = new OrderIsOverdue()
        .not();

List<Order> urgent = orders.stream()
        .filter(needsAttention::isSatisfiedBy)
        .toList();
```
