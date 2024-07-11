package com.credibanco.bancinc.repositoryTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.credibanco.bancinc.model.Customer;
import com.credibanco.bancinc.repository.CustomerRepository;

@DataJpaTest
public class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;

    @BeforeEach
    void setup() {
        customer = Customer.builder()
                .id(1L)
                .name("David")
                .lastName("Alfonso")
                .email("jdavid.alfonso@gmail.com")
                .build();
    }

    @DisplayName("Test para guardar cliente")
    @Test
    void testSaveCustomer() {
        // BDD
        // -> given: condicion previa o configuracion
        Customer customer1 = customer;

        // -> when: Accion o comportamiento que vamos a probar
        Customer customerSaved = customerRepository.save(customer1);

        // -> then: verificacion de dalida
        assertThat(customerSaved).isNotNull();
        assertThat(customer1.getId()).isGreaterThan(0);
    }

    @DisplayName("Test para optener datos de un cliente por su email")
    @Test
    public void testGetDataByEmail() {
        // BDD
        // -> given: condicion previa o configuracion
        Customer customer1 = customer;
        customerRepository.save(customer1);

        // -> when: Accion o comportamiento que vamos a probar
        Customer customerBD = customerRepository.findByEmail(customer1.getEmail()).get();

        // -> then: verificacion de dalida
        assertThat(customerBD).isNotNull();
        assertThat(customerBD.getEmail()).isEqualTo(customer.getEmail());
        assertThat(customerBD.getName()).isEqualTo(customer.getName());
    }
}
