package com.credibanco.bancinc.service;

import org.springframework.http.ResponseEntity;

import com.credibanco.bancinc.dto.ResponseDTO;
import com.credibanco.bancinc.model.Customer;

public interface CustomerService {

    ResponseEntity<ResponseDTO> saveCustomer(Customer customer);

    ResponseEntity<ResponseDTO> listCustomers();

    ResponseEntity<ResponseDTO> getCustomerById(long customerID);

    ResponseEntity<ResponseDTO> getCustomerByEmail(String email);

    ResponseEntity<ResponseDTO> deleteCustomer(long customerID);

}
