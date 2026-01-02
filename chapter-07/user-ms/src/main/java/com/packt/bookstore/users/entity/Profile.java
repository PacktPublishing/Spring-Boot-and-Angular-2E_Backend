package com.packt.bookstore.users.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Profile {

    private String fullName;

    private String birthDate; // Use String or LocalDate

    private String phone;

    private Address address;
}
