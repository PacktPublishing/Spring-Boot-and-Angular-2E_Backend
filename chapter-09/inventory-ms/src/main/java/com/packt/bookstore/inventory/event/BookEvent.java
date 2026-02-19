package com.packt.bookstore.inventory.event;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base event class for book-related notifications.
 * Provides common fields for all book events sent via SSE.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookEvent {
    
    /**
     * Type of the event (NEW_BOOK, PRICE_CHANGE, etc.)
     */
    private EventType eventType;
    
    /**
     * Timestamp when the event occurred
     */
    private LocalDateTime timestamp;
    
    /**
     * Unique identifier for the event
     */
    private String eventId;
    
    /**
     * The book ID associated with this event
     */
    private Long bookId;
    
    /**
     * Title of the book
     */
    private String bookTitle;
    
    /**
     * ISBN of the book
     */
    private String isbn;
    
    /**
     * Additional event-specific data
     */
    private Object eventData;
    
    /**
     * Enum defining types of book events
     */
    public enum EventType {
        NEW_BOOK,
        PRICE_CHANGE,
        BOOK_UPDATED,
        BOOK_DELETED
    }
}
