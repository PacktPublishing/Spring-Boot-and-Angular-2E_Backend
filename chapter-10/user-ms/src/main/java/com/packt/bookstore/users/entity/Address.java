package com.packt.bookstore.users.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Address {

    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
