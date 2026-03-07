package com.packt.bookstore.users.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Preferences {

    private String language;   // e.g., "en", "fr"
    private Boolean newsletter;
    private String currency;   // e.g., "USD", "EUR"

    private String [] favoriteGenres; // e.g., ["Fiction", "Science Fiction"]
}