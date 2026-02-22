package com.packt.bookstore.inventory.event;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Specific data for PRICE_CHANGE events.
 * Contains old and new price information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceChangeEventData {
    
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private BigDecimal priceChange;
    private Double percentageChange;
}
