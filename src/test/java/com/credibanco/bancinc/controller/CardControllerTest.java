package com.credibanco.bancinc.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.credibanco.bancinc.dto.CardDTO;
import com.credibanco.bancinc.dto.ResponseDTO;
import com.credibanco.bancinc.dto.ResponseErrorDTO;
import com.credibanco.bancinc.model.Card;
import com.credibanco.bancinc.model.Customer;
import com.credibanco.bancinc.service.CardService;
import com.credibanco.bancinc.utils.UniqueNumberGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CardController.class)
public class CardControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        private Customer customer;

        private Card card;

        private CardDTO cardDTO;

        private UniqueNumberGenerator numRandomCard;

        @MockBean
        private CardService cardService;

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

                cardDTO = new CardDTO(card);

        }

        @Test
        public void testGetCardById_Created() throws Exception {
                // >Given
                ResponseDTO responseDTO = new ResponseDTO(HttpStatus.CREATED.value(), card);
                when(cardService.saveCard(anyInt())).thenReturn(new ResponseEntity<>(responseDTO, HttpStatus.CREATED));

                // > When
                ResultActions response = mockMvc
                                .perform(MockMvcRequestBuilders.get("/card/{productId}/number",
                                                card.getNumProductCard()));

                // >Then
                response.andExpect(status().isOk())
                                .andDo(print());
        }

        @Test
        public void testGetCardById_NotAcceptable() throws Exception {
                // Given
                card.setNumProductCard(1234567);
                ResponseErrorDTO responseErrorDTO = new ResponseErrorDTO(HttpStatus.NOT_ACCEPTABLE.value(), null,
                                "El numero de tarjeta no se creo: 123");
                when(cardService.saveCard(anyInt()))
                                .thenReturn(new ResponseEntity<>(responseErrorDTO, HttpStatus.NOT_ACCEPTABLE));

                // When
                ResultActions response = mockMvc.perform(
                                MockMvcRequestBuilders.get("/card/{productId}/number", card.getNumProductCard()));

                // Then
                response.andExpect(status().isNotAcceptable())
                                .andDo(print())
                                .andExpect(jsonPath("$.errorMsn")
                                                .value("El numero de tarjeta no se creo: " + card.getNumProductCard()));
        }

        @Test
        void testEnableCard_Accepted() throws Exception {
                // >Given
                ResponseDTO responseDTO = new ResponseDTO(HttpStatus.ACCEPTED.value(), card);
                when(cardService.enableCard(cardDTO))
                                .thenReturn(new ResponseEntity<>(responseDTO, HttpStatus.ACCEPTED));

                // > When
                ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/card/enroll")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cardDTO)));

                // >Then
                response.andExpect(status().isAccepted())
                                .andDo(print());
        }

        @Test
        void testDeleteCard() throws Exception {
                // > Given
                String cardNumber = "1234562374436970";

                ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), "Eliminado con exito");
                given(cardService.deleteCard(cardNumber)).willReturn(ResponseEntity.ok(responseDTO));

                // > When
                ResultActions response = mockMvc.perform(delete("/card/{cardId}", cardNumber)
                                .contentType(MediaType.APPLICATION_JSON));

                // > Then
                response.andExpect(status().isOk())
                                .andDo(print())
                                .andExpect(jsonPath("$.statusCode", is(HttpStatus.OK.value())))
                                .andExpect(jsonPath("$.data", is("Eliminado con exito")));
        }

        @Test
        void testUpdateBalance_Success() throws Exception {

                // >Given
                ResponseDTO responseDTO = new ResponseDTO(HttpStatus.ACCEPTED.value(), card);
                when(cardService.updateBalance(cardDTO))
                                .thenReturn(new ResponseEntity<>(responseDTO, HttpStatus.ACCEPTED));

                // > When
                ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/card/balance")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cardDTO)));

                // >Then
                response.andExpect(status().isAccepted())
                                .andDo(print());
        }
}
