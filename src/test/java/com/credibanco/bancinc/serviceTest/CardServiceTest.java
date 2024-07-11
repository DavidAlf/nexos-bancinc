package com.credibanco.bancinc.serviceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.credibanco.bancinc.dto.CardDTO;
import com.credibanco.bancinc.dto.ResponseDTO;
import com.credibanco.bancinc.dto.ResponseErrorDTO;
import com.credibanco.bancinc.model.Card;
import com.credibanco.bancinc.model.Customer;
import com.credibanco.bancinc.repository.CardRepository;
import com.credibanco.bancinc.service.impl.CardServiceImpl;
import com.credibanco.bancinc.utils.UniqueNumberGenerator;

public class CardServiceTest {
        @Mock
        private CardRepository cardRepository;

        @InjectMocks
        private CardServiceImpl cardService;

        private Customer customer;

        private Card card;

        private UniqueNumberGenerator numRandomCard;

        public CardServiceTest() {
                MockitoAnnotations.openMocks(this);
        }

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

        @Test
        void testSaveCard() {
                // > Given
                given(cardRepository.findByNumProductCardAndNumRandomCard(card.getNumProductCard(),
                                card.getNumRandomCard()))
                                .willReturn(Optional.empty());
                given(cardRepository.save(card)).willReturn(card);

                // // >Then
                ResponseEntity<ResponseDTO> cardSave = cardService.saveCard(card.getNumProductCard());

                // // >When
                assertThat(cardSave).isNotNull();
        }

        @SuppressWarnings("null")
        @Test
        void testSaveCard_ExceptionHandlingCaught() {
                // Given
                Card card = new Card();
                card.setNumProductCard(123456);
                card.setNumRandomCard(123456789L);

                given(cardRepository.findByNumProductCardAndNumRandomCard(card.getNumProductCard(),
                                card.getNumRandomCard()))
                                .willReturn(Optional.empty());
                doThrow(new RuntimeException("Database error")).when(cardRepository).save(any(Card.class));

                // When
                ResponseEntity<ResponseDTO> response = cardService.saveCard(card.getNumProductCard());

                // Then
                verify(cardRepository, times(1)).save(any(Card.class));

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertTrue(response.getBody() instanceof ResponseErrorDTO);
                ResponseErrorDTO errorDTO = (ResponseErrorDTO) response.getBody();
                assertEquals("Error guardando la tarjeta", errorDTO.getErrorMsn());
        }

        @Test
        void testGetDataByID() {
                // >Given
                given(cardRepository.findById(customer.getId())).willReturn(Optional.of(card));

                // >Then
                ResponseEntity<ResponseDTO> cardSave = cardService.getCardById(card.getId());

                // >When
                assertThat(cardSave).isNotNull();
        }

        @SuppressWarnings("null")
        @Test
        void testGetCardById_ExceptionHandling() {
                // Given
                doThrow(new RuntimeException("Database error")).when(cardRepository).findById(card.getId());

                // When
                ResponseEntity<ResponseDTO> response = cardService.getCardById(card.getId());

                // Then
                verify(cardRepository, times(1)).findById(card.getId());
                assert (response.getStatusCode() == HttpStatus.NOT_FOUND);
                assert (response.getBody() instanceof ResponseErrorDTO);
                ResponseErrorDTO responseError = (ResponseErrorDTO) response.getBody();
                assert (responseError.getErrorMsn().equals("Error la tarjeta " + card.getId()));
        }

        @SuppressWarnings("null")
        @Test
        public void testEnableCard_CardExists() {
                // Given
                String cardNumber = "1234562374436970";
                Card card = new Card();
                card.setStatus("disabled");

                CardDTO cardDTO = new CardDTO(card);

                String numProductCard = cardNumber.substring(0, 6);
                String numRandomCard = cardNumber.substring(6, 16);

                given(cardRepository.findByNumProductCardAndNumRandomCard(
                                Integer.parseInt(numProductCard), Long.parseLong(numRandomCard)))
                                .willReturn(Optional.of(card));

                // When
                ResponseEntity<ResponseDTO> response = cardService.enableCard(cardNumber);

                // Then
                verify(cardRepository).save(card);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals("enable", card.getStatus());
                CardDTO res = (CardDTO) response.getBody().getData();
                assertEquals(cardDTO.getCardId(), res.getCardId());
        }

        @SuppressWarnings("null")
        @Test
        public void testEnableCard_CardNotFound() {
                // Given
                String cardNumber = "1234562374436970";
                String numProductCard = cardNumber.substring(0, 6);
                String numRandomCard = cardNumber.substring(6, 16);

                given(cardRepository.findByNumProductCardAndNumRandomCard(
                                Integer.parseInt(numProductCard), Long.parseLong(numRandomCard)))
                                .willReturn(Optional.empty());

                // When
                ResponseEntity<ResponseDTO> response = cardService.enableCard(cardNumber);

                // Then
                verify(cardRepository).findByNumProductCardAndNumRandomCard(
                                Integer.parseInt(numProductCard), Long.parseLong(numRandomCard));
                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertEquals("El numero de tarjeta no existe: " + cardNumber,
                                ((ResponseErrorDTO) response.getBody()).getErrorMsn());
        }

        @SuppressWarnings("null")
        @Test
        public void testEnableCard_ExceptionHandling() {
                // Given
                String cardNumber = "invalidCardNumber";

                // When
                ResponseEntity<ResponseDTO> response = cardService.enableCard(cardNumber);

                // Then
                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertEquals("El numero de tarjeta no existe: " + cardNumber,
                                ((ResponseErrorDTO) response.getBody()).getErrorMsn());
        }

        @Test
        void testDeleteCard() {
                // >Given
                long cardID = 1L;
                willDoNothing().given(cardRepository).deleteById(cardID);

                // >When
                cardRepository.deleteById(cardID);

                // >Then
                verify(cardRepository, times(1)).deleteById(cardID);
        }

        @SuppressWarnings("null")
        @Test
        void testDeleteCarc_CardNotFound() {
                // Given
                String cardNumber = "1234562374436970";

                String numProductCard = cardNumber.substring(0, 6);
                String numRandomCard = cardNumber.substring(6, 16);

                when(cardRepository.findByNumProductCardAndNumRandomCard(Integer.parseInt(numProductCard),
                                Long.parseLong(numRandomCard))).thenReturn(Optional.empty());

                // When
                ResponseEntity<ResponseDTO> response = cardService.deleteCard(cardNumber);

                // Then
                verify(cardRepository, times(1)).findByNumProductCardAndNumRandomCard(Integer.parseInt(numProductCard),
                                Long.parseLong(numRandomCard));
                verify(cardRepository, never()).deleteById(anyLong());

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertTrue(response.getBody() instanceof ResponseErrorDTO);
                ResponseErrorDTO responseError = (ResponseErrorDTO) response.getBody();
                assertEquals("El id de la tarjeta no existe: " + cardNumber, responseError.getErrorMsn());
        }

        @SuppressWarnings("null")
        @Test
        public void testUpdateBalance_CardExists() {
                // Given
                String cardNumber = "1234562374436970";
                int balance = 1000;
                Card card = new Card();
                card.setBalance(0);

                CardDTO cardDTO = new CardDTO(card);

                String numProductCard = cardNumber.substring(0, 6);
                String numRandomCard = cardNumber.substring(6, 16);

                given(cardRepository.findByNumProductCardAndNumRandomCard(
                                Integer.parseInt(numProductCard), Long.parseLong(numRandomCard)))
                                .willReturn(Optional.of(card));

                // When
                ResponseEntity<ResponseDTO> response = cardService.updateBalance(cardNumber, balance);

                // Then
                verify(cardRepository).save(card);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(balance, card.getBalance());
                CardDTO rest = (CardDTO) response.getBody().getData();
                assertEquals(cardDTO.getCardId(), rest.getCardId());
        }

        @SuppressWarnings("null")
        @Test
        public void testUpdateBalance_CardNotFound() {
                // Given
                String cardNumber = "1234562374436970";
                int balance = 1000;
                String numProductCard = cardNumber.substring(0, 6);
                String numRandomCard = cardNumber.substring(6, 16);

                given(cardRepository.findByNumProductCardAndNumRandomCard(
                                Integer.parseInt(numProductCard), Long.parseLong(numRandomCard)))
                                .willReturn(Optional.empty());

                // When
                ResponseEntity<ResponseDTO> response = cardService.updateBalance(cardNumber, balance);

                // Then
                verify(cardRepository).findByNumProductCardAndNumRandomCard(
                                Integer.parseInt(numProductCard), Long.parseLong(numRandomCard));
                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertEquals("El numero de tarjeta no existe: " + cardNumber,
                                ((ResponseErrorDTO) response.getBody()).getErrorMsn());
        }

        @SuppressWarnings("null")
        @Test
        public void testUpdateBalance_ExceptionHandling() {
                // Given
                String cardNumber = "invalidCardNumber";
                int balance = 1000;

                // When
                ResponseEntity<ResponseDTO> response = cardService.updateBalance(cardNumber, balance);

                // Then
                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertEquals("El numero de tarjeta no existe: " + cardNumber,
                                ((ResponseErrorDTO) response.getBody()).getErrorMsn());
        }

}