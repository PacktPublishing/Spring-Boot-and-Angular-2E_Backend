package com.packt.bookstore.inventory.event;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Specific data for NEW_BOOK events.
 * Contains comprehensive information about newly added books.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewBookEventData {
    
    private String authorName;
    private String genre;
    private BigDecimal price;
    private Integer quantity;
    private LocalDate published;
    private String description;
    private Integer pageCount;
    private String coverImageUrl;
}
