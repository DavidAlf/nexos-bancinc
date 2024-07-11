package com.credibanco.bancinc.dto;

import com.credibanco.bancinc.model.Customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CustomerDTO {

    private String name;

    private String lastName;

    private String email;

    public CustomerDTO(Customer customer) {
        this.name = customer.getName();
        this.lastName = customer.getLastName();
        this.email = customer.getEmail();
    }
}
