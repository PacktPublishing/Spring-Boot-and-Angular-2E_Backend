package com.packt.bookstore.users.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Document(collection = "users")
@Data
@Builder
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String keycloakId;

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String username;

    private LocalDateTime dateOfBirth;

    private Profile profile;

    private Preferences preferences;

    private LocalDateTime createdAt;

    private LocalDateTime lastLogin;

}
