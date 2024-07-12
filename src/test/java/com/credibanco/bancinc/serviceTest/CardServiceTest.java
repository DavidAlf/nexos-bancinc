package com.credibanco.bancinc.serviceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

        private CardDTO cardDTO;

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
                                .balance(100)
                                .build();

                cardDTO = new CardDTO(card);

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
        void testSaveCard_InvalidProductId() {
                // Given
                card.setNumProductCard(12345);

                // When
                ResponseEntity<ResponseDTO> response = cardService.saveCard(card.getNumProductCard());

                // Then
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                assertTrue(response.getBody() instanceof ResponseErrorDTO);
                ResponseErrorDTO errorDTO = (ResponseErrorDTO) response.getBody();
                assertEquals("El número de producto debe tener exactamente 6 dígitos: " + card.getNumProductCard(),
                                errorDTO.getErrorMsn());

        }

        @SuppressWarnings("null")
        @Test
        void testSaveCard_ExceptionHandlingCaught() {
                // Given
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
        public void testEnableCard_ValidCard() {

                given(cardRepository.findByNumProductCardAndNumRandomCard(card.getNumProductCard(),
                                card.getNumRandomCard()))
                                .willReturn(Optional.of(card));

                ResponseEntity<ResponseDTO> response = cardService.enableCard(cardDTO);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertTrue(response.getBody().getData() instanceof CardDTO);
        }

        @SuppressWarnings("null")
        @Test
        public void testEnableCard_InvalidCardId_Length() {
                cardDTO.setCardId("123");
                given(cardRepository.findByNumProductCardAndNumRandomCard(card.getNumProductCard(),
                                card.getNumRandomCard()))
                                .willReturn(Optional.of(card));

                ResponseEntity<ResponseDTO> response = cardService.enableCard(cardDTO);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                assertTrue(response.getBody() instanceof ResponseErrorDTO);
                ResponseErrorDTO errorDTO = (ResponseErrorDTO) response.getBody();
                assertEquals("ID de tarjeta debe tener exactamente 16 dígitos numéricos", errorDTO.getErrorMsn());

        }

        @SuppressWarnings("null")
        @Test
        public void testEnableCard_InvalidCardId_NonNumeric() {
                cardDTO.setCardId("1234abcd56789012");

                ResponseEntity<ResponseDTO> response = cardService.enableCard(cardDTO);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                assertTrue(response.getBody() instanceof ResponseErrorDTO);
                ResponseErrorDTO errorDTO = (ResponseErrorDTO) response.getBody();
                assertEquals("ID de tarjeta debe tener exactamente 16 dígitos numéricos", errorDTO.getErrorMsn());
        }

        @SuppressWarnings("null")
        @Test
        public void testEnableCard_CardNotFound() {
                cardDTO.setCardId("1234567890123456");

                given(cardRepository.findByNumProductCardAndNumRandomCard(123456, 7890123456L))
                                .willReturn(Optional.empty());

                ResponseEntity<ResponseDTO> response = cardService.enableCard(cardDTO);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertTrue(response.getBody() instanceof ResponseErrorDTO);
                ResponseErrorDTO errorDTO = (ResponseErrorDTO) response.getBody();
                assertEquals("El numero de tarjeta no existe: 1234567890123456", errorDTO.getErrorMsn());
        }

        @SuppressWarnings("null")
        @Test
        public void testEnableCard_Exception() {
                cardDTO.setCardId("1234567890123456");

                given(cardRepository.findByNumProductCardAndNumRandomCard(123456, 7890123456L))
                                .willThrow(new RuntimeException("Database error"));

                ResponseEntity<ResponseDTO> response = cardService.enableCard(cardDTO);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertTrue(response.getBody() instanceof ResponseErrorDTO);
                ResponseErrorDTO errorDTO = (ResponseErrorDTO) response.getBody();
                assertEquals("Error en el consumo enableCard", errorDTO.getErrorMsn());
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
        public void testDeleteCard_InvalidCardId_Length() {
                given(cardRepository.findByNumProductCardAndNumRandomCard(card.getNumProductCard(),
                                card.getNumRandomCard()))
                                .willReturn(Optional.of(card));

                ResponseEntity<ResponseDTO> response = cardService.deleteCard("12345678");

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                assertTrue(response.getBody() instanceof ResponseErrorDTO);
                ResponseErrorDTO errorDTO = (ResponseErrorDTO) response.getBody();
                assertEquals("ID de tarjeta debe tener exactamente 16 dígitos numéricos", errorDTO.getErrorMsn());

        }

        @SuppressWarnings("null")
        @Test
        public void testDeleteCard_CardNotFound() {
                cardDTO.setCardId("1234567890123456");

                given(cardRepository.findByNumProductCardAndNumRandomCard(123456, 7890123456L))
                                .willReturn(Optional.empty());

                ResponseEntity<ResponseDTO> response = cardService.deleteCard(cardDTO.getCardId());

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertTrue(response.getBody() instanceof ResponseErrorDTO);
                ResponseErrorDTO errorDTO = (ResponseErrorDTO) response.getBody();
                assertEquals("El numero de la tarjeta no existe: 1234567890123456", errorDTO.getErrorMsn());
        }

        @SuppressWarnings("null")
        @Test
        public void testDeleteCard_Exception() {
                cardDTO.setCardId("1234567890123456");

                given(cardRepository.findByNumProductCardAndNumRandomCard(123456, 7890123456L))
                                .willThrow(new RuntimeException("Database error"));

                ResponseEntity<ResponseDTO> response = cardService.deleteCard(cardDTO.getCardId());

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertTrue(response.getBody() instanceof ResponseErrorDTO);
                ResponseErrorDTO errorDTO = (ResponseErrorDTO) response.getBody();
                assertEquals("Error en el consumo deleteCard", errorDTO.getErrorMsn());
        }

        @SuppressWarnings("null")
        @Test
        public void testUpdateBalance_ValidCard() {

                given(cardRepository.findByNumProductCardAndNumRandomCard(card.getNumProductCard(),
                                card.getNumRandomCard()))
                                .willReturn(Optional.of(card));

                ResponseEntity<ResponseDTO> response = cardService.updateBalance(cardDTO);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertNotNull(response.getBody());
                assertTrue(response.getBody().getData() instanceof CardDTO);
        }

        @SuppressWarnings("null")
        @Test
        public void testUpdateBalance_InvalidCardId_Length() {
                cardDTO.setCardId("123");
                given(cardRepository.findByNumProductCardAndNumRandomCard(card.getNumProductCard(),
                                card.getNumRandomCard()))
                                .willReturn(Optional.of(card));

                ResponseEntity<ResponseDTO> response = cardService.updateBalance(cardDTO);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                assertTrue(response.getBody() instanceof ResponseErrorDTO);
                ResponseErrorDTO errorDTO = (ResponseErrorDTO) response.getBody();
                assertEquals("ID de tarjeta debe tener exactamente 16 dígitos numéricos", errorDTO.getErrorMsn());
        }

        @SuppressWarnings("null")
        @Test
        public void testUpdateBalance_InvalidCardId_NonNumeric() {
                cardDTO.setCardId("1234abcd56789012");
                given(cardRepository.findByNumProductCardAndNumRandomCard(card.getNumProductCard(),
                                card.getNumRandomCard()))
                                .willReturn(Optional.of(card));

                ResponseEntity<ResponseDTO> response = cardService.updateBalance(cardDTO);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                assertTrue(response.getBody() instanceof ResponseErrorDTO);
                ResponseErrorDTO errorDTO = (ResponseErrorDTO) response.getBody();
                assertEquals("ID de tarjeta debe tener exactamente 16 dígitos numéricos", errorDTO.getErrorMsn());
        }

        @SuppressWarnings("null")
        @Test
        public void testUpdateBalance_InvalidBalance_NonPositive() {
                card.setBalance(-50);
                cardDTO.setBalance(card.getBalance());
                given(cardRepository.findByNumProductCardAndNumRandomCard(card.getNumProductCard(),
                                card.getNumRandomCard()))
                                .willReturn(Optional.of(card));

                ResponseEntity<ResponseDTO> response = cardService.updateBalance(cardDTO);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                assertTrue(response.getBody() instanceof ResponseErrorDTO);
                ResponseErrorDTO errorDTO = (ResponseErrorDTO) response.getBody();
                assertEquals("Debe insertar el monto de la tarjeta positivo [blanace]", errorDTO.getErrorMsn());
        }

        @SuppressWarnings("null")
        @Test
        public void testUpdateBalance_CardNotFound() {
                card.setNumProductCard(123457);
                given(cardRepository.findByNumProductCardAndNumRandomCard(card.getNumProductCard(),
                                card.getNumRandomCard()))
                                .willReturn(Optional.of(card));

                ResponseEntity<ResponseDTO> response = cardService.updateBalance(cardDTO);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertTrue(response.getBody() instanceof ResponseErrorDTO);
                ResponseErrorDTO errorDTO = (ResponseErrorDTO) response.getBody();
                assertEquals("El numero de tarjeta no existe: " + cardDTO.getCardId(), errorDTO.getErrorMsn());
        }

        @SuppressWarnings("null")
        @Test
        public void testUpdateBalance_Exception() {
                cardDTO.setBalance(100);
                given(cardRepository.findByNumProductCardAndNumRandomCard(card.getNumProductCard(),
                                card.getNumRandomCard()))
                                .willThrow(new RuntimeException("Database error"));

                ResponseEntity<ResponseDTO> response = cardService.updateBalance(cardDTO);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
                assertTrue(response.getBody() instanceof ResponseErrorDTO);
                ResponseErrorDTO errorDTO = (ResponseErrorDTO) response.getBody();
                assertEquals("Error en el consumo updateBalance", errorDTO.getErrorMsn());
        }

}