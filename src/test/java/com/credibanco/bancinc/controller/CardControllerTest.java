package com.credibanco.bancinc.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.credibanco.bancinc.dto.ResponseDTO;
import com.credibanco.bancinc.model.Card;
import com.credibanco.bancinc.service.CardService;

@WebMvcTest(CardController.class)
public class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @Test
    void testGetCardById_Success() throws Exception {
        // Given
        int productId = 123456; // 6 dígitos
        Card card = new Card();
        card.setId(1L);
        card.setNumProductCard(productId);
        card.setNumRandomCard(123456789L);
        card.setExpDate(LocalDate.now().plusYears(1));
        card.setStatus("enable");

        // Mock behavior for saveCard
        ResponseDTO saveCardResponseDTO = new ResponseDTO(HttpStatus.OK.value(), card);
        given(cardService.saveCard(productId)).willReturn(ResponseEntity.ok(saveCardResponseDTO));

        // Mock behavior for getCardById
        ResponseDTO getCardByIdResponseDTO = new ResponseDTO(HttpStatus.OK.value(), card);
        given(cardService.getCardById(card.getId())).willReturn(ResponseEntity.ok(getCardByIdResponseDTO));

        // When
        ResultActions response = mockMvc.perform(get("/card/{productId}/number", productId));

        // Then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.numProductCard", is(productId)))
                .andExpect(jsonPath("$.data.numRandomCard", is(123456789)))
                .andExpect(jsonPath("$.data.status", is("enable")));
    }

    @Test
    void testGetCardById_CardNotFound() throws Exception {
        // Given
        int productId = 123456; // 6 dígitos
        ResponseDTO responseDTO = new ResponseDTO(HttpStatus.NOT_FOUND.value(), null);

        // Mock behavior for saveCard
        given(cardService.saveCard(productId))
                .willReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDTO));

        // When
        ResultActions response = mockMvc.perform(get("/card/{productId}/number", productId));

        // Then
        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.errorMsn", is("El numero de tarjeta no existe: " + productId)));
    }

    @Test
    void testGetCardById_DataIsNull() throws Exception {
        // Given
        int productId = 123456; // 6 dígitos
        ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), null); // Datos nulos

        // Mock behavior for saveCard
        given(cardService.saveCard(productId)).willReturn(ResponseEntity.ok(responseDTO));

        // When
        ResultActions response = mockMvc.perform(get("/card/{productId}/number", productId));

        // Then
        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.errorMsn", is("El numero de tarjeta no existe: " + productId)));
    }
}
