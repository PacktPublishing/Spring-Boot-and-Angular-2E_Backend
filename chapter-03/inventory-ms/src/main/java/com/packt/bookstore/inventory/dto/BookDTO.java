package com.packt.bookstore.inventory.dto;

import java.math.BigDecimal;

public record BookDTO(Long id, String title, String authorName, BigDecimal price) {

}
