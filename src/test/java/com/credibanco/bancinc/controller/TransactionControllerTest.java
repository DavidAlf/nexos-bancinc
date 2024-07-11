package com.credibanco.bancinc.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.credibanco.bancinc.dto.PurcharseDTO;
import com.credibanco.bancinc.dto.ResponseDTO;
import com.credibanco.bancinc.model.Card;
import com.credibanco.bancinc.model.Customer;
import com.credibanco.bancinc.model.Transaction;
import com.credibanco.bancinc.service.TransactionService;
import com.credibanco.bancinc.utils.UniqueNumberGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private TransactionService tansactionService;

        private Customer customer;

        private Card card;

        private UniqueNumberGenerator numRandomCard;

        private Transaction transaction;

        private PurcharseDTO purchaseDTO;

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

                transaction = Transaction.builder()
                                .id(1L)
                                .card(card)
                                .expDate(LocalDate.now())
                                .status("acepted")
                                .build();

                purchaseDTO = PurcharseDTO.builder()
                                .cardId("1234567890123456")
                                .price(100)
                                .transactionId("100")
                                .build();

        }

        @Test
        void testSaveTransaction() throws Exception {
                // > Given
                given(tansactionService.saveTransaction(purchaseDTO)).willAnswer((invocation) -> {
                        ResponseDTO responseDTO = new ResponseDTO(HttpStatus.CREATED.value(), transaction);
                        return responseDTO;
                });

                // > When
                ResultActions response = mockMvc.perform(post("/transaction/purchase")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(transaction)));

                // > Then
                response.andDo(print())
                                .andExpect(status().isCreated());
        }

        @Test
        void testGetDataById() throws Exception {
                // >Given
                ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), transaction);

                given(tansactionService.getTransactionById(transaction.getId()))
                                .willReturn(ResponseEntity.ok(responseDTO));

                // >When
                ResultActions response = mockMvc.perform(get("/transaction/{transactionId}", transaction.getId()));

                // >Then
                response.andExpect(status().isOk())
                                .andDo(print())
                                .andExpect(jsonPath("$.data.price", is(transaction.getPrice())))
                                .andExpect(jsonPath("$.data.status", is(transaction.getStatus())))
                                .andExpect(jsonPath("$.data.card.numRandomCard", is(transaction.getCard().getNumRandomCard())));
        }

        @Test
        void testAnulationTransaction() throws Exception {
                // > Given
                given(tansactionService.saveTransaction(purchaseDTO)).willAnswer((invocation) -> {
                        ResponseDTO responseDTO = new ResponseDTO(HttpStatus.CREATED.value(), transaction);
                        return responseDTO;
                });

                // > When
                ResultActions response = mockMvc.perform(post("/transaction/anulation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(transaction)));

                // > Then
                response.andDo(print())
                                .andExpect(status().isCreated());
        }
}
