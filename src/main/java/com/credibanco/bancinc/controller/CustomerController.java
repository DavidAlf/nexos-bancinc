package com.credibanco.bancinc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.credibanco.bancinc.dto.ResponseDTO;
import com.credibanco.bancinc.model.Customer;
import com.credibanco.bancinc.service.CustomerService;

import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ResponseDTO> saveCountry(@RequestBody @Validated Customer customer) {
        log.info("[CustomerController] -> saveCountry");

        return customerService.saveCustomer(customer);
    }

    @GetMapping
    public ResponseEntity<ResponseDTO> listCustomers() {
        log.info("[CustomerController] -> listCustomers");

        return customerService.listCustomers();
    }

    @GetMapping("/{customerID}")
    public ResponseEntity<ResponseDTO> getCustomerById(@PathVariable("customerID") long customerID) {
        log.info("[CustomerController] -> getCustomerById [" + customerID + "]");

        return customerService.getCustomerById(customerID);
    }

    @GetMapping("/find")
    public ResponseEntity<ResponseDTO> getCustomerByEmail(@PathParam("email") String email) {
        log.info("[CustomerController] -> getCustomerByEmail [" + email + "]");

        return customerService.getCustomerByEmail(email);
    }

    @DeleteMapping("/{customerID}")
    public ResponseEntity<ResponseDTO> eliminaEmpleado(@PathVariable("customerID") long customerID) {
        log.info("[CustomerController] -> eliminaEmpleado [" + customerID + "]");

        return customerService.deleteCustomer(customerID);

    }
}
