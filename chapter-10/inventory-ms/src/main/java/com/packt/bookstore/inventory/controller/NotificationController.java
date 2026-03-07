package com.packt.bookstore.inventory.controller;

import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.packt.bookstore.inventory.event.BookEvent;
import com.packt.bookstore.inventory.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * REST Controller for Server-Sent Events (SSE) notifications.
 * Provides reactive endpoints for clients to subscribe to real-time book updates.
 * 
 * SSE is ideal for one-way server-to-client communication where the server
 * pushes updates to connected clients automatically.
 */
@Slf4j
@Tag(name = "Notifications", description = "Server-Sent Events (SSE) endpoints for real-time book notifications")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(
        summary = "Subscribe to all book events via SSE",
        description = """
            Establishes a Server-Sent Events (SSE) connection to receive real-time notifications
            about book-related events including:
            - NEW_BOOK: When a new book is added to inventory
            - PRICE_CHANGE: When a book's price is updated
            - BOOK_UPDATED: When book details are modified
            - BOOK_DELETED: When a book is removed from inventory
            
            The connection remains open and events are pushed as they occur.
            Clients should implement automatic reconnection logic.
            
            Usage example with JavaScript:
            ```javascript
            const eventSource = new EventSource('/api/notifications/stream');
            eventSource.onmessage = (event) => {
                const bookEvent = JSON.parse(event.data);
                console.log('Received event:', bookEvent);
            };
            eventSource.onerror = (error) => {
                console.error('SSE error:', error);
                eventSource.close();
            };
            ```
            
            Usage example with curl:
            ```bash
            curl -N -H "Accept: text/event-stream" http://localhost:8081/inventory/api/notifications/stream
            ```
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "SSE connection established successfully",
            content = @Content(
                mediaType = "text/event-stream",
                schema = @Schema(implementation = BookEvent.class),
                examples = @ExampleObject(
                    name = "SSE Event Example",
                    value = """
                        data: {
                        data:   "eventType": "NEW_BOOK",
                        data:   "timestamp": "2026-02-15T10:30:00",
                        data:   "eventId": "550e8400-e29b-41d4-a716-446655440000",
                        data:   "bookId": 42,
                        data:   "bookTitle": "Reactive Spring",
                        data:   "isbn": "978-1234567890",
                        data:   "eventData": {
                        data:     "authorName": "Josh Long",
                        data:     "genre": "Technology",
                        data:     "price": 49.99,
                        data:     "quantity": 100
                        data:   }
                        data: }
                        
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error - SSE stream terminated unexpectedly"
        )
    })
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<BookEvent>> streamAllEvents() {
        log.info("New SSE client connected to /stream endpoint");
        
        // Create keepalive heartbeat every 15 seconds
        Flux<ServerSentEvent<BookEvent>> heartbeat = Flux.interval(Duration.ofSeconds(15))
                .map(sequence -> ServerSentEvent.<BookEvent>builder()
                        .comment("keepalive")
                        .build());
        
        // Merge event stream with heartbeat
        Flux<ServerSentEvent<BookEvent>> eventStream = notificationService.getEventStream()
                .map(event -> ServerSentEvent.<BookEvent>builder()
                        .id(event.getEventId())
                        .event(event.getEventType().toString())
                        .data(event)
                        .comment("Book notification event")
                        .build());
        
        return Flux.merge(eventStream, heartbeat)
                .doOnCancel(() -> log.info("SSE client disconnected from /stream"))
                .doOnError(error -> log.error("Error in SSE stream", error))
                .timeout(Duration.ofHours(1)) // Auto-disconnect after 1 hour of inactivity
                .onErrorResume(error -> {
                    log.error("Fatal error in SSE stream, closing connection", error);
                    return Flux.empty();
                });
    }

    @Operation(
        summary = "Subscribe to specific event type via SSE",
        description = """
            Similar to /stream but filters events by type. Only events matching
            the specified type will be sent to the client.
            
            Available event types:
            - NEW_BOOK: Notifications about newly added books
            - PRICE_CHANGE: Notifications about price updates
            - BOOK_UPDATED: Notifications about book detail updates
            - BOOK_DELETED: Notifications about book deletions
            
            Example:
            ```bash
            curl -N -H "Accept: text/event-stream" \
                 "http://localhost:8081/inventory/api/notifications/stream/filtered?eventType=PRICE_CHANGE"
            ```
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "SSE connection established with filter applied",
            content = @Content(
                mediaType = "text/event-stream",
                schema = @Schema(implementation = BookEvent.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid event type specified"
        )
    })
    @GetMapping(value = "/stream/filtered", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<BookEvent>> streamFilteredEvents(
            @Parameter(
                name = "eventType",
                description = "Type of events to receive (NEW_BOOK, PRICE_CHANGE, BOOK_UPDATED, BOOK_DELETED)",
                required = true,
                example = "NEW_BOOK"
            )
            @RequestParam BookEvent.EventType eventType
    ) {
        log.info("New SSE client connected to /stream/filtered endpoint with filter: {}", eventType);
        
        // Create keepalive heartbeat every 15 seconds
        Flux<ServerSentEvent<BookEvent>> heartbeat = Flux.interval(Duration.ofSeconds(15))
                .map(sequence -> ServerSentEvent.<BookEvent>builder()
                        .comment("keepalive")
                        .build());
        
        // Merge filtered event stream with heartbeat
        Flux<ServerSentEvent<BookEvent>> eventStream = notificationService.getEventStreamByType(eventType)
                .map(event -> ServerSentEvent.<BookEvent>builder()
                        .id(event.getEventId())
                        .event(event.getEventType().toString())
                        .data(event)
                        .comment("Filtered book notification event")
                        .build());
        
        return Flux.merge(eventStream, heartbeat)
                .doOnCancel(() -> log.info("SSE client disconnected from /stream/filtered"))
                .doOnError(error -> log.error("Error in filtered SSE stream", error))
                .timeout(Duration.ofHours(1))
                .onErrorResume(error -> {
                    log.error("Fatal error in filtered SSE stream, closing connection", error);
                    return Flux.empty();
                });
    }

    @Operation(
        summary = "Get notification service health status",
        description = """
            Returns the current status of the notification service including
            the number of active SSE subscribers. Useful for monitoring and debugging.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Service status retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "status": "UP",
                          "activeSubscribers": 5,
                          "message": "Notification service is operating normally"
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/status")
    public NotificationStatus getStatus() {
        int activeSubscribers = notificationService.getActiveSubscriberCount();
        log.debug("Notification status requested. Active subscribers: {}", activeSubscribers);
        
        return new NotificationStatus(
                "UP",
                activeSubscribers,
                "Notification service is operating normally"
        );
    }

    /**
     * DTO for notification service status response
     */
    public record NotificationStatus(
            String status,
            int activeSubscribers,
            String message
    ) {}
}
