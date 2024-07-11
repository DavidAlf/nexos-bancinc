package com.credibanco.bancinc.repositoryTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.credibanco.bancinc.utils.UniqueNumberGenerator;
import com.credibanco.bancinc.model.Card;
import com.credibanco.bancinc.model.Customer;
import com.credibanco.bancinc.repository.CardRepository;

@DataJpaTest
public class CardRepositoryTest {

    @Autowired
    private CardRepository cardRepository;

    private Card card;

    private Customer customer;

    private UniqueNumberGenerator numRandomCard;

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

    }

    @DisplayName("Test para guardar una tarjeta")
    @Test
    void testSaveCard() {
        // BDD
        // -> given: condicion previa o configuracion
        numRandomCard = new UniqueNumberGenerator();

        Card card1 = card;

        // -> when: Accion o comportamiento que vamos a probar
        Card cardSaved = cardRepository.save(card1);

        // -> then: verificacion de dalida
        assertThat(cardSaved).isNotNull();
        assertThat(card1.getId()).isGreaterThan(0);
    }

    @DisplayName("Test para optener datos de unatarjeta por sus numeros")
    @Test
    public void testGetDataByCardNumber() {
        // BDD
        // -> given: condicion previa o configuracion
        Card card1 = card;
        cardRepository.save(card1);

        // -> when: Accion o comportamiento que vamos a probar
        Card cardBD = cardRepository
                .findByNumProductCardAndNumRandomCard(card1.getNumProductCard(), card1.getNumRandomCard()).get();

        // -> then: verificacion de dalida
        assertThat(cardBD).isNotNull();
        assertThat(cardBD.getNumProductCard()).isEqualTo(card.getNumProductCard());
        assertThat(cardBD.getNumRandomCard()).isEqualTo(card.getNumRandomCard());
        assertThat(cardBD.getCustomer().getId()).isEqualTo(card.getCustomer().getId());
    }
}
