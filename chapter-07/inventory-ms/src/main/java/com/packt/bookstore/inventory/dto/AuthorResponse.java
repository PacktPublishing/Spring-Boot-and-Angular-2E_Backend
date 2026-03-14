package com.packt.bookstore.inventory.dto;

import java.util.List;

public record AuthorResponse(
        Long id,
        String name,
        String nationality,
        List<String> books) {
}
