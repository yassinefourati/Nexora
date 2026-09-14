package com.fourati.platform.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Thin wrapper around ApplicationEventPublisher.
 *
 * Why wrap it?
 *   - Adds structured logging so every published event appears in the log
 *   - Single injection point — services depend on DomainEventPublisher, not Spring's interface
 *   - Easy to swap with a message broker (Kafka, RabbitMQ) later without touching services
 *
 * Usage in a service:
 *   private final DomainEventPublisher eventPublisher;
 *
 *   eventPublisher.publish(new ItemCreatedEvent(item.getId(), item.getName()));
 *
 * Listening to events:
 *   @Component
 *   public class ItemEventListener {
 *
 *       @EventListener
 *       public void on(ItemCreatedEvent event) {
 *           // runs synchronously in the same thread
 *       }
 *
 *       @Async
 *       @EventListener
 *       public void onAsync(ItemCreatedEvent event) {
 *           // runs in the async thread pool (fire-and-forget)
 *       }
 *
 *       @TransactionalEventListener(phase = AFTER_COMMIT)
 *       public void onAfterCommit(ItemCreatedEvent event) {
 *           // only fires AFTER the DB transaction commits — safe for external calls
 *       }
 *   }
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publish(DomainEvent event) {
        log.debug("Publishing event: {}", event);
        publisher.publishEvent(event);
    }

}
