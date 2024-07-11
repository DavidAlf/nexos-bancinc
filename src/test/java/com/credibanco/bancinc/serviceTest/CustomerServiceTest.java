package com.credibanco.bancinc.serviceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.credibanco.bancinc.dto.ResponseDTO;
import com.credibanco.bancinc.dto.ResponseErrorDTO;
import com.credibanco.bancinc.model.Card;
import com.credibanco.bancinc.model.Customer;
import com.credibanco.bancinc.repository.CardRepository;
import com.credibanco.bancinc.repository.CustomerRepository;
import com.credibanco.bancinc.service.impl.CustomerServiceImpl;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;

    public CustomerServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeEach
    void setup() {
        customer = Customer.builder()
                .id(1L)
                .name("David")
                .lastName("Alfonso")
                .email("jdavid.alfonso@gmail.com")
                .build();
    }

    
    @Test
    void testSaveCustomer() {
        // > Given
        given(customerRepository.findByEmail(customer.getEmail())).willReturn(Optional.empty());
        given(customerRepository.save(customer)).willReturn(customer);

        // // >Then
        ResponseEntity<ResponseDTO> customerSave = customerService.saveCustomer(customer);

        // // >When
        assertThat(customerSave).isNotNull();
    }

    @Test
    void testSaveCustomer_ExceptionCaught() {
        // Given
        given(customerRepository.findByEmail(customer.getEmail())).willReturn(Optional.empty());
        doThrow(new RuntimeException("Database error")).when(customerRepository).save(customer);

        // When
        ResponseEntity<ResponseDTO> response = customerService.saveCustomer(customer);

        // Then
        verify(customerRepository, times(1)).findByEmail(customer.getEmail());
        verify(customerRepository, times(1)).save(customer);
        assert (response.getStatusCode() == HttpStatus.NOT_FOUND);
        assert (response.getBody() instanceof ResponseErrorDTO);
        assert (((ResponseErrorDTO) response.getBody()).getErrorMsn().equals("Error guardando el cliente"));
    }

    
    @Test
    void testListCustomers() {
        // >Given
        Customer customer1 = Customer.builder()
                .id(1L)
                .name("Cristian")
                .lastName("Tovar")
                .email("Cristian.Tovar@gmail.com")
                .build();

        List<Customer> listCustomers = new ArrayList<Customer>();
        listCustomers.add(customer);
        listCustomers.add(customer1);

        given(customerRepository.findAll()).willReturn(listCustomers);

        // >Then
        ResponseEntity<ResponseDTO> listCustomersDto = customerService.listCustomers();
        Object obj = listCustomersDto.getBody().getData();

        // >When
        assertThat(listCustomersDto).isNotNull();
        if (obj instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<Customer> listCustomerSaved = (List<Customer>) obj;

            assertThat(listCustomerSaved.size()).isEqualTo(2);
        }

    }

    @Test
    void testListCustomersEmpty() {
        // >Given
        given(customerRepository.findAll()).willReturn(Collections.emptyList());

        // >When
        ResponseEntity<ResponseDTO> listCustomersDto = customerService.listCustomers();
        Object obj = listCustomersDto.getBody().getData();

        // >When
        assertThat(listCustomersDto).isNotNull();
        if (obj instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<Customer> listCustomerSaved = (List<Customer>) obj;

            assertThat(listCustomerSaved).isEmpty();
            assertThat(listCustomerSaved.size()).isEqualTo(0);
        }

    }

    @Test
    void testListCustomers_Exception() {
        // Given
        doThrow(new RuntimeException("Database error")).when(customerRepository).findAll();

        // When
        ResponseEntity<ResponseDTO> response = customerService.listCustomers();

        // Then
        verify(customerRepository, times(1)).findAll();
        assert (response.getStatusCode() == HttpStatus.NOT_FOUND);
        assert (response.getBody() instanceof ResponseErrorDTO);
        assert (((ResponseErrorDTO) response.getBody()).getErrorMsn().equals("Error listando el clientes"));
    }

    
    @Test
    void testGetDataByID() {
        // >Given
        given(customerRepository.findById(customer.getId())).willReturn(Optional.of(customer));

        // >Then
        ResponseEntity<ResponseDTO> customerSave = customerService.getCustomerById(customer.getId());

        // >When
        assertThat(customerSave).isNotNull();
    }

    @Test
    void testGetCustomerById_Exception() {
        // Given
        long customerID = 3L;
        doThrow(new RuntimeException("Database error")).when(customerRepository).findById(customerID);

        // When
        ResponseEntity<ResponseDTO> response = customerService.getCustomerById(customerID);

        // Then
        verify(customerRepository, times(1)).findById(customerID);
        assert (response.getStatusCode() == HttpStatus.NOT_FOUND);
        assert (response.getBody() instanceof ResponseErrorDTO);
        ResponseErrorDTO responseError = (ResponseErrorDTO) response.getBody();
        assert (responseError.getErrorMsn().equals("Error buscando el cliente " + customerID));
    }

    
    @Test
    void testGetDataByEmail() {
        // >Given
        given(customerRepository.findByEmail(customer.getEmail())).willReturn(Optional.of(customer));

        // >Then
        ResponseEntity<ResponseDTO> customerSave = customerService.getCustomerByEmail(customer.getEmail());

        // >When
        assertThat(customerSave).isNotNull();
    }

    @Test
    void testGetCustomerByEmail_Exception() {
        // Given
        String customerEmail = "jdavid.alfonso@gmail.com";
        doThrow(new RuntimeException("Database error")).when(customerRepository).findByEmail(customerEmail);

        // When
        ResponseEntity<ResponseDTO> response = customerService.getCustomerByEmail(customerEmail);

        // Then
        verify(customerRepository, times(1)).findByEmail(customerEmail);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ResponseErrorDTO);
        ResponseErrorDTO responseError = (ResponseErrorDTO) response.getBody();
        assertEquals("Error buscando el cliente " + customerEmail, responseError.getErrorMsn());
    }

    @Test
    void testDeleteCustomer() {
        // >Given
        long customerId = 1L;
        willDoNothing().given(customerRepository).deleteById(customerId);

        // >When
        customerRepository.deleteById(customerId);

        // >Then
        verify(customerRepository, times(1)).deleteById(customerId);
    }

    @Test
    void testDeleteCustomer_CustomerNotFound() {
        // Given
        long customerID = 3L;
        when(customerRepository.findById(customerID)).thenReturn(Optional.empty());

        // When
        ResponseEntity<ResponseDTO> response = customerService.deleteCustomer(customerID);

        // Then
        verify(customerRepository, times(1)).findById(customerID);
        verify(cardRepository, never()).findByCustomerId(anyLong());
        verify(customerRepository, never()).deleteById(anyLong());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ResponseErrorDTO);
        ResponseErrorDTO responseError = (ResponseErrorDTO) response.getBody();
        assertEquals("El id del cliente no existe: " + customerID, responseError.getErrorMsn());
    }

    @Test
    void testDeleteCustomer_CustomerHasCards() {
        // Given
        long customerID = 3L;
        Customer customer = new Customer();
        customer.setId(customerID);
        List<Card> cards = new ArrayList<>();
        cards.add(new Card()); // Simulamos que el cliente tiene tarjetas asociadas

        when(customerRepository.findById(customerID)).thenReturn(Optional.of(customer));
        when(cardRepository.findByCustomerId(customerID)).thenReturn(cards);

        // When
        ResponseEntity<ResponseDTO> response = customerService.deleteCustomer(customerID);

        // Then
        verify(customerRepository, times(1)).findById(customerID);
        verify(cardRepository, times(1)).findByCustomerId(customerID);
        verify(customerRepository, never()).deleteById(anyLong());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ResponseErrorDTO);
        ResponseErrorDTO responseError = (ResponseErrorDTO) response.getBody();
        assertEquals("El cliente tiene tarjetas asociadas: " + cards.size(), responseError.getErrorMsn());
    }

}
