package com.credibanco.bancinc.serviceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
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

import com.credibanco.bancinc.dto.PurcharseDTO;
import com.credibanco.bancinc.dto.ResponseDTO;
import com.credibanco.bancinc.dto.ResponseErrorDTO;
import com.credibanco.bancinc.dto.TransactionDTO;
import com.credibanco.bancinc.model.Card;
import com.credibanco.bancinc.model.Customer;
import com.credibanco.bancinc.model.Transaction;
import com.credibanco.bancinc.repository.CardRepository;
import com.credibanco.bancinc.repository.TransactionRepository;
import com.credibanco.bancinc.service.CardService;
import com.credibanco.bancinc.service.impl.TransactionServiceImpl;
import com.credibanco.bancinc.utils.UniqueNumberGenerator;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardService cardService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Customer customer;

    private Card card;

    private UniqueNumberGenerator numRandomCard;

    private Transaction transaction;

    private PurcharseDTO purchaseDTO;

    public TransactionServiceTest() {
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
                .balance(1000)
                .build();

        purchaseDTO = PurcharseDTO.builder()
                .cardId("1234567890123456")
                .price(100)
                .transactionId("1")
                .build();

        transaction = Transaction.builder()
                .id(1L)
                .card(card)
                .expDate(LocalDate.now())
                .status("acepted")
                .price(purchaseDTO.getPrice())
                .build();
    }

    @SuppressWarnings("null")
    @Test
    public void testSaveTransaction_Success() {
        // Given
        given(cardRepository.findByNumProductCardAndNumRandomCard(123456, 7890123456L)).willReturn(Optional.of(card));
        given(transactionRepository.save(transaction)).willReturn(transaction);

        // Then
        ResponseEntity<ResponseDTO> transactionSave = transactionService.saveTransaction(purchaseDTO);
        TransactionDTO expectedTransactionDTO = new TransactionDTO(transaction);

        ResponseDTO expectedResponseDTO = new ResponseDTO(HttpStatus.OK.value(), expectedTransactionDTO);

        // // >When
        assertThat(transactionSave).isNotNull();
        TransactionDTO responseTransactionDTO = (TransactionDTO) expectedResponseDTO.getData();
        assertThat(responseTransactionDTO).isNotNull();
        assertThat(responseTransactionDTO.getTransactionId()).isEqualTo(expectedTransactionDTO.getTransactionId());
        assertThat(responseTransactionDTO.getPrice()).isEqualTo(purchaseDTO.getPrice());
    }

    @SuppressWarnings("null")
    @Test
    public void testSaveTransaction_CardNotFound() {
        // Given
        given(cardRepository.findByNumProductCardAndNumRandomCard(123456, 7890123456L)).willReturn(Optional.empty());

        // When
        ResponseEntity<ResponseDTO> response = transactionService.saveTransaction(purchaseDTO);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ResponseErrorDTO responseError = (ResponseErrorDTO) response.getBody();
        assertEquals("El numero de tarjeta no existe: 1234567890123456", responseError.getErrorMsn());
    }

    @SuppressWarnings("null")
    @Test
    public void testSaveTransaction_CardExpired() {
        // Given
        card.setExpDate(LocalDate.now().minusDays(1)); // Fecha de expiración pasada

        given(cardRepository.findByNumProductCardAndNumRandomCard(123456, 7890123456L)).willReturn(Optional.of(card));

        // When
        ResponseEntity<ResponseDTO> response = transactionService.saveTransaction(purchaseDTO);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ResponseErrorDTO responseError = (ResponseErrorDTO) response.getBody();
        assertEquals("La tarjeta esta vencida: fecha expira[" + card.getExpDate() + "]", responseError.getErrorMsn());
    }

    @SuppressWarnings("null")
    @Test
    public void testSaveTransaction_InvalidCardStatus() {
        // Given
        card.setStatus("disabled");

        given(cardRepository.findByNumProductCardAndNumRandomCard(123456, 7890123456L)).willReturn(Optional.of(card));

        // When
        ResponseEntity<ResponseDTO> response = transactionService.saveTransaction(purchaseDTO);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ResponseErrorDTO responseError = (ResponseErrorDTO) response.getBody();
        assertEquals("La tarjeta tiene estatus invalido: estatus[disabled] tiene que estar con estatus [enable]",
                responseError.getErrorMsn());
    }

    @SuppressWarnings("null")
    @Test
    public void testSaveTransaction_CardNotBalance() {
        // Given
        card.setBalance(0);
        given(cardRepository.findByNumProductCardAndNumRandomCard(123456, 7890123456L)).willReturn(Optional.of(card));

        // When
        ResponseEntity<ResponseDTO> response = transactionService.saveTransaction(purchaseDTO);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ResponseErrorDTO responseError = (ResponseErrorDTO) response.getBody();
        assertEquals("La tarjeta no tiene fondos suficientes: fondo[0] < precio [100]", responseError.getErrorMsn());
    }

    @Test
    void testGetDataByID() {
        // >Given
        given(transactionRepository.findById(transaction.getId())).willReturn(Optional.of(transaction));

        // >Then
        ResponseEntity<ResponseDTO> transactionSave = transactionService.getTransactionById(transaction.getId());

        // >When
        assertThat(transactionSave).isNotNull();
    }

    @SuppressWarnings("null")
    @Test
    void testGetDataByID_Exception() {
        // Given
        doThrow(new RuntimeException("Database error")).when(transactionRepository).findById(transaction.getId());

        // When
        ResponseEntity<ResponseDTO> response = transactionService.getTransactionById(transaction.getId());

        // Then
        verify(transactionRepository, times(1)).findById(transaction.getId());
        assert (response.getStatusCode() == HttpStatus.NOT_FOUND);
        assert (response.getBody() instanceof ResponseErrorDTO);
        ResponseErrorDTO responseError = (ResponseErrorDTO) response.getBody();
        assert (responseError.getErrorMsn().equals("Error buscando la transaccion " + transaction.getId()));
    }

    @SuppressWarnings("null")
    @Test
    public void testAnulationTransaction_Success() {
        // Given
        given(cardRepository.findByNumProductCardAndNumRandomCard(123456, 7890123456L)).willReturn(Optional.of(card));
        given(transactionRepository.findById(transaction.getId())).willReturn(Optional.of(transaction));

        // Then
        ResponseEntity<ResponseDTO> transactionSave = transactionService.anulationTransaction(purchaseDTO);
        TransactionDTO expectedTransactionDTO = new TransactionDTO(transaction);

        ResponseDTO expectedResponseDTO = new ResponseDTO(HttpStatus.OK.value(), expectedTransactionDTO);

        // // >When
        assertThat(transactionSave).isNotNull();
        TransactionDTO responseTransactionDTO = (TransactionDTO) expectedResponseDTO.getData();
        assertThat(responseTransactionDTO).isNotNull();
        assertThat(responseTransactionDTO.getTransactionId()).isEqualTo(expectedTransactionDTO.getTransactionId());
        assertThat(responseTransactionDTO.getPrice()).isEqualTo(purchaseDTO.getPrice());
    }

    @SuppressWarnings("null")
    @Test
    public void testAnulationTransaction_CardNotFound() {
        // Given
        given(cardRepository.findByNumProductCardAndNumRandomCard(123456, 7890123456L)).willReturn(Optional.empty());

        // When
        ResponseEntity<ResponseDTO> response = transactionService.anulationTransaction(purchaseDTO);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ResponseErrorDTO responseError = (ResponseErrorDTO) response.getBody();
        assertEquals("El numero de tarjeta no existe: 1234567890123456", responseError.getErrorMsn());
    }

    @SuppressWarnings("null")
    @Test
    public void testAnulationTransaction_TransactionNotFound() {
        // Given
        transaction.setId(100L);
        given(cardRepository.findByNumProductCardAndNumRandomCard(123456, 7890123456L)).willReturn(Optional.of(card));

        // When
        ResponseEntity<ResponseDTO> response = transactionService.anulationTransaction(purchaseDTO);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ResponseErrorDTO responseError = (ResponseErrorDTO) response.getBody();
        assertEquals("El numero de transaccion no existe: " + purchaseDTO.getTransactionId(),
                responseError.getErrorMsn());
    }

    @SuppressWarnings("null")
    @Test
    public void testAnulationTransaction_TransactionIsAnnulled() {
        // Given
        transaction.setStatus("annulled");
        given(cardRepository.findByNumProductCardAndNumRandomCard(123456, 7890123456L)).willReturn(Optional.of(card));
        given(transactionRepository.findById(transaction.getId())).willReturn(Optional.of(transaction));

        // When
        ResponseEntity<ResponseDTO> response = transactionService.anulationTransaction(purchaseDTO);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ResponseErrorDTO responseError = (ResponseErrorDTO) response.getBody();
        assertEquals("La transaccion ya se encuentra anulada", responseError.getErrorMsn());
    }

    @SuppressWarnings("null")
    @Test
    public void testAnulationTransaction_ExtDateException() {
        // Given
        transaction.setExpDate(LocalDate.now().plusDays(10));
        given(cardRepository.findByNumProductCardAndNumRandomCard(123456, 7890123456L)).willReturn(Optional.of(card));
        given(transactionRepository.findById(transaction.getId())).willReturn(Optional.of(transaction));

        // When
        ResponseEntity<ResponseDTO> response = transactionService.anulationTransaction(purchaseDTO);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ResponseErrorDTO responseError = (ResponseErrorDTO) response.getBody();
        assertEquals("La transaccion ya no se puede anular por que tiene mas de 24 horas", responseError.getErrorMsn());
    }

}
