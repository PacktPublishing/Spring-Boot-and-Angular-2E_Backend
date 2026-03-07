package com.packt.bookstore.inventory.service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

import com.packt.bookstore.inventory.event.BookEvent;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

/**
 * Service responsible for managing Server-Sent Events (SSE) notifications.
 * Uses Project Reactor's Sinks to broadcast book-related events to multiple subscribers.
 * 
 * This service implements a pub-sub pattern where:
 * - Publishers (BookService) emit events via publishEvent()
 * - Subscribers (SSE clients) receive events via getEventStream()
 */
@Service
@Slf4j
public class NotificationService {

    /**
     * Sink for broadcasting book events to all subscribers.
     * Many-to-Many: Multiple publishers can emit, multiple subscribers can listen.
     * LATEST strategy: If subscriber is slow, it gets only the latest event (backpressure handling)
     */
    private final Many<BookEvent> bookEventSink;
    
    /**
     * Flux that subscribers can tap into to receive events
     */
    private final Flux<BookEvent> bookEventFlux;
    
    /**
     * Track active subscribers for monitoring and debugging
     */
    private final ConcurrentMap<String, LocalDateTime> activeSubscribers;

    public NotificationService() {
        // Initialize active subscribers map first
        this.activeSubscribers = new ConcurrentHashMap<>();
        
        // Create a multicast sink that can be safely shared across threads
        // onBackpressureBuffer: Buffer events if subscriber can't keep up (buffer size: 256)
        this.bookEventSink = Sinks.many().multicast().onBackpressureBuffer(256);
        
        // Create a hot flux from the sink - events are broadcast to all active subscribers
        this.bookEventFlux = bookEventSink.asFlux()
                .doOnSubscribe(subscription -> {
                    String subscriberId = UUID.randomUUID().toString();
                    activeSubscribers.put(subscriberId, LocalDateTime.now());
                    log.info("New SSE subscriber connected. Total subscribers: {}", activeSubscribers.size());
                })
                .doOnCancel(() -> {
                    log.info("SSE subscriber cancelled. Remaining subscribers: {}", activeSubscribers.size());
                })
                .doOnError(error -> {
                    log.error("Error in SSE stream", error);
                })
                .share(); // Share the flux among multiple subscribers
        
        log.info("NotificationService initialized with reactive SSE support");
    }

    /**
     * Publishes a book event to all connected SSE subscribers.
     * This method is thread-safe and can be called from multiple threads.
     * 
     * @param event The book event to broadcast
     */
    public void publishEvent(BookEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }
        
        log.info("Publishing {} event for book ID: {} ({})", 
                event.getEventType(), event.getBookId(), event.getBookTitle());
        
        // Emit the event to all subscribers
        Sinks.EmitResult result = bookEventSink.tryEmitNext(event);
        
        if (result.isFailure()) {
            log.warn("Failed to emit event: {} - Reason: {}", event.getEventType(), result);
        } else {
            log.debug("Successfully emitted event to {} active subscribers", activeSubscribers.size());
        }
    }

    /**
     * Returns a Flux stream of book events for SSE subscribers.
     * Each subscriber gets their own subscription to the shared flux.
     * 
     * @return Flux of BookEvent objects
     */
    public Flux<BookEvent> getEventStream() {
        return bookEventFlux;
    }

    /**
     * Returns a Flux stream filtered by event type.
     * 
     * @param eventType The type of events to receive
     * @return Flux of filtered BookEvent objects
     */
    public Flux<BookEvent> getEventStreamByType(BookEvent.EventType eventType) {
        return bookEventFlux
                .filter(event -> event.getEventType() == eventType)
                .doOnNext(event -> 
                    log.debug("Filtering event: {} for type: {}", event.getEventId(), eventType)
                );
    }

    /**
     * Get the number of active SSE subscribers.
     * Useful for monitoring and metrics.
     * 
     * @return Number of active subscribers
     */
    public int getActiveSubscriberCount() {
        return activeSubscribers.size();
    }
}
