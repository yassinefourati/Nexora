package com.fourati.platform.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events.
 *
 * Domain events decouple modules:
 *   ItemService publishes ItemCreatedEvent
 *   NotificationService, AuditService, EmailService each listen independently
 *   → No direct dependency between them
 *
 * Events are synchronous by default (same thread).
 * Add @Async to a listener to make it non-blocking.
 *
 * Example:
 *   public final class ItemCreatedEvent extends DomainEvent {
 *       private final UUID itemId;
 *       private final String name;
 *
 *       public ItemCreatedEvent(UUID itemId, String name) {
 *           this.itemId = itemId;
 *           this.name   = name;
 *       }
 *       // getters
 *   }
 */
public abstract class DomainEvent {

	private final UUID eventId = UUID.randomUUID();
	private final Instant occurredAt = Instant.now();
	private final String actor;

	protected DomainEvent(String actor) {
		this.actor = actor;
	}

	protected DomainEvent() {
		this.actor = "system";
	}

	public UUID getEventId() {
		return eventId;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}

	public String getActor() {
		return actor;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{eventId=" + eventId + ", actor='" + actor + "', occurredAt=" + occurredAt
				+ '}';
	}
}
