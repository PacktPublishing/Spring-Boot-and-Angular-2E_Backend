package com.packt.bookstore.users.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Profile {

    private String firstName;

    private String lastName;

    private LocalDateTime birthDate; // Use String or LocalDate

    private String phone;

    private Address address;

    private String profilePictureUrl;


}
