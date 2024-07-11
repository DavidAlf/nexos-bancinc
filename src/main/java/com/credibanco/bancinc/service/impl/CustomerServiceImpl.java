package com.credibanco.bancinc.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.credibanco.bancinc.dto.ResponseDTO;
import com.credibanco.bancinc.dto.ResponseErrorDTO;
import com.credibanco.bancinc.exeptions.ResourceNotFundExcepton;
import com.credibanco.bancinc.model.Card;
import com.credibanco.bancinc.model.Customer;
import com.credibanco.bancinc.repository.CardRepository;
import com.credibanco.bancinc.repository.CustomerRepository;
import com.credibanco.bancinc.service.CustomerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CardRepository cardRepository;

    @Override
    public ResponseEntity<ResponseDTO> saveCustomer(Customer customer) {
        log.info("[CustomerServiceImpl] -> saveCustomer");

        try {
            Optional<Customer> customerSaved = customerRepository.findByEmail(customer.getEmail());

            if (customerSaved.isPresent()) {
                throw new ResourceNotFundExcepton("El email del cliente ya existe: " + customerSaved.get().getEmail());
            }

            customerRepository.save(customer);

            ResponseDTO responseDTO = new ResponseDTO(HttpStatus.CREATED.value(), customer);

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            log.error("Error saveCustomer " + e.getMessage());
            ResponseErrorDTO responseErrorDTO = new ResponseErrorDTO(HttpStatus.NOT_FOUND.value(),
                    new Customer(), "Error guardando el cliente");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErrorDTO);
        }
    }

    @Override
    public ResponseEntity<ResponseDTO> listCustomers() {
        log.info("[CustomerServiceImpl] -> listCustomers");
        try {
            List<Customer> listCustomers = customerRepository.findAll();

            ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), listCustomers);

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            log.error("Error saveCustomer " + e.getMessage());
            ResponseErrorDTO responseErrorDTO = new ResponseErrorDTO(HttpStatus.NOT_FOUND.value(),
                    new ArrayList<Customer>(), "Error listando el clientes");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErrorDTO);
        }
    }

    @Override
    public ResponseEntity<ResponseDTO> getCustomerById(long customerID) {
        log.info("[CustomerServiceImpl] -> getCustomer");
        try {
            Optional<Customer> customer = customerRepository.findById(customerID);

            ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), customer);

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            log.error("Error getCustomer " + e.getMessage());
            ResponseErrorDTO responseErrorDTO = new ResponseErrorDTO(HttpStatus.NOT_FOUND.value(),
                    new Customer(), "Error buscando el cliente " + customerID);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErrorDTO);
        }
    }

    @Override
    public ResponseEntity<ResponseDTO> getCustomerByEmail(String email) {
        log.info("[CustomerServiceImpl] -> getCustomer");
        try {
            Optional<Customer> customer = customerRepository.findByEmail(email);

            ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), customer);

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            log.error("Error saveCustomer " + e.getMessage());
            ResponseErrorDTO responseErrorDTO = new ResponseErrorDTO(HttpStatus.NOT_FOUND.value(),
                    new Customer(), "Error buscando el cliente " + email);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErrorDTO);
        }
    }

    @Override
    public ResponseEntity<ResponseDTO> deleteCustomer(long customerID) {
        log.info("[CustomerServiceImpl] -> deleteCustomer");
        String msn = "";
        try {
            Optional<Customer> customer = customerRepository.findById(customerID);

            if (!customer.isPresent()) {
                msn = "El id del cliente no existe: " + customerID;
                throw new ResourceNotFundExcepton(msn);
            }

            List<Card> cards = cardRepository.findByCustomerId(customer.get().getId());

            if (cards.size() > 0) {
                msn = "El cliente tiene tarjetas asociadas: " + cards.size();
                throw new ResourceNotFundExcepton(msn);
            }
            customerRepository.deleteById(customerID);

            ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), "Eliminado con exito");

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            log.error("Error deleteCustomer " + e.getMessage());
            ResponseErrorDTO responseErrorDTO = new ResponseErrorDTO(HttpStatus.NOT_FOUND.value(),
                    null, msn);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErrorDTO);
        }
    }

}
