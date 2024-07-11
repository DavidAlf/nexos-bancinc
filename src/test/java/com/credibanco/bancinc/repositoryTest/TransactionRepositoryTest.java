package com.credibanco.bancinc.repositoryTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.credibanco.bancinc.model.Card;
import com.credibanco.bancinc.model.Customer;
import com.credibanco.bancinc.model.Transaction;
import com.credibanco.bancinc.repository.TransactionRepository;
import com.credibanco.bancinc.utils.UniqueNumberGenerator;

@DataJpaTest
public class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    private Card card;

    private Customer customer;

    private UniqueNumberGenerator numRandomCard;

    private Transaction transaction;

    @BeforeEach
    void setup() {
        numRandomCard = new UniqueNumberGenerator();

        customer = Customer.builder()
                .id(1L)
                .name("David")
                .lastName("Alfonso")
                .email("jdavid.alfonso@gmail.com")
                .build();

        card = Card.builder()
                .id(1L)
                .numProductCard(123456)
                .numRandomCard(numRandomCard.generateUniqueNumber())
                .expDate(LocalDate.of(2024, 11, 11))
                .customer(customer)
                .status("enable")
                .build();

        transaction = Transaction.builder()
                .id(1L)
                .expDate(LocalDate.of(2024, 10, 18))
                .card(card)
                .status("pending")
                .build();
    }

    @DisplayName("Test para guardar una transaccion")
    @Test
    void testSaveTransaction() {
        // BDD
        // -> given: condicion previa o configuracion
        Transaction transaction1 = transaction;
        
        // -> when: Accion o comportamiento que vamos a probar
        Transaction transactionSaved = transactionRepository.save(transaction1);

        // -> then: verificacion de dalida
        assertThat(transactionSaved).isNotNull();
        assertThat(transaction1.getId()).isGreaterThan(0);
    }

}
