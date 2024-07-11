package com.credibanco.bancinc.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

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
                ResultActions response = mockMvc
                                .perform(MockMvcRequestBuilders.get("/card/{productId}/number", productId));

                // Then
                response.andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.data.id", is(1)))
                                .andExpect(MockMvcResultMatchers.jsonPath("$.data.numProductCard", is(productId)))
                                .andExpect(MockMvcResultMatchers.jsonPath("$.data.numRandomCard", is(123456789)))
                                .andExpect(MockMvcResultMatchers.jsonPath("$.data.status", is("enable")))
                                .andDo(MockMvcResultHandlers.print());
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
                ResultActions response = mockMvc
                                .perform(MockMvcRequestBuilders.get("/card/{productId}/number", productId));

                // Then
                response.andExpect(MockMvcResultMatchers.status().isNotFound())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode",
                                                is(HttpStatus.NOT_FOUND.value())))
                                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMsn",
                                                is("El numero de tarjeta no existe: " + productId)))
                                .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testGetCardById_DataIsNull() throws Exception {
                // Given
                int productId = 123456; // 6 dígitos
                ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), null); // Datos nulos

                // Mock behavior for saveCard
                given(cardService.saveCard(productId)).willReturn(ResponseEntity.ok(responseDTO));

                // When
                ResultActions response = mockMvc
                                .perform(MockMvcRequestBuilders.get("/card/{productId}/number", productId));

                // Then
                response.andExpect(MockMvcResultMatchers.status().isNotFound())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode",
                                                is(HttpStatus.NOT_FOUND.value())))
                                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMsn",
                                                is("El numero de tarjeta no existe: " + productId)))
                                .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testGetCardById_InvalidProductId() throws Exception {
                // Given
                int productId = 12345; // Menos de 6 dígitos

                // When
                ResultActions response = mockMvc
                                .perform(MockMvcRequestBuilders.get("/card/{productId}/number", productId));

                // Then
                response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode",
                                                is(HttpStatus.BAD_REQUEST.value())))
                                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMsn",
                                                is("El número de producto debe tener exactamente 6 dígitos: "
                                                                + productId)))
                                .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testGetCardById_InvalidProductId_TooLong() throws Exception {
                // Given
                int productId = 1234567; // Más de 6 dígitos

                // When
                ResultActions response = mockMvc
                                .perform(MockMvcRequestBuilders.get("/card/{productId}/number", productId));

                // Then
                response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode",
                                                is(HttpStatus.BAD_REQUEST.value())))
                                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMsn",
                                                is("El número de producto debe tener exactamente 6 dígitos: "
                                                                + productId)))
                                .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testEnableCard_Success() throws Exception {
                // Given
                String cardId = "1234567890123456"; // 16 dígitos
                ResponseDTO responseDTO = new ResponseDTO(HttpStatus.CREATED.value(),
                                "Tarjeta habilitada exitosamente");

                // Mock behavior for enableCard
                given(cardService.enableCard(cardId))
                                .willReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseDTO));

                // When
                ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/card/enroll")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"cardId\":\"" + cardId + "\"}"));

                // Then
                response.andExpect(status().isCreated())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode",
                                                is(HttpStatus.CREATED.value())))
                                .andExpect(MockMvcResultMatchers.jsonPath("$.data",
                                                is("Tarjeta habilitada exitosamente")))
                                .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testEnableCard_InvalidCardId_LengthLessThan16() throws Exception {
                // Given
                String cardId = "12345678901234"; // Menos de 16 dígitos

                // When
                ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/card/enroll")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"cardId\":\"" + cardId + "\"}"));

                // Then
                response.andExpect(status().isBadRequest())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode",
                                                is(HttpStatus.BAD_REQUEST.value())))
                                .andExpect(MockMvcResultMatchers.jsonPath("$.data",
                                                is("ID de tarjeta debe tener exactamente 16 dígitos numéricos")))
                                .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testEnableCard_InvalidCardId_LengthMoreThan16() throws Exception {
                // Given
                String cardId = "12345678901234567"; // Más de 16 dígitos

                // When
                ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/card/enroll")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"cardId\":\"" + cardId + "\"}"));

                // Then
                response.andExpect(status().isBadRequest())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode",
                                                is(HttpStatus.BAD_REQUEST.value())))
                                .andExpect(MockMvcResultMatchers.jsonPath("$.data",
                                                is("ID de tarjeta debe tener exactamente 16 dígitos numéricos")))
                                .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testEnableCard_InvalidCardId_NonNumeric() throws Exception {
                // Given
                String cardId = "12345678901234AB"; // Contiene caracteres no numéricos

                // When
                ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/card/enroll")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"cardId\":\"" + cardId + "\"}"));

                // Then
                response.andExpect(status().isBadRequest())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode",
                                                is(HttpStatus.BAD_REQUEST.value())))
                                .andExpect(MockMvcResultMatchers.jsonPath("$.data",
                                                is("ID de tarjeta debe tener exactamente 16 dígitos numéricos")))
                                .andDo(MockMvcResultHandlers.print());
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
                // Given
                String cardId = "1234567890123456"; // 16 dígitos
                String balance = "1000";
                ResponseDTO responseDTO = new ResponseDTO(HttpStatus.CREATED.value(),
                                "Balance actualizado exitosamente");

                // Mock behavior for updateBalance
                given(cardService.updateBalance(cardId, Integer.parseInt(balance)))
                                .willReturn(ResponseEntity.status(HttpStatus.CREATED).body(responseDTO));

                // When
                ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/card/balance")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"cardId\":\"" + cardId + "\", \"balance\":\"" + balance + "\"}"));

                // Then
                response.andExpect(status().isCreated())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode",
                                                is(HttpStatus.CREATED.value())))
                                .andExpect(MockMvcResultMatchers.jsonPath("$.data",
                                                is("Balance actualizado exitosamente")))
                                .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testUpdateBalance_InvalidCardId() throws Exception {
                // Given
                String cardId = "12345678901234"; // Menos de 16 dígitos
                String balance = "1000";

                // When
                ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/card/balance")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"cardId\":\"" + cardId + "\", \"balance\":\"" + balance + "\"}"));

                // Then
                response.andExpect(status().isBadRequest())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode",
                                                is(HttpStatus.BAD_REQUEST.value())))
                                .andExpect(MockMvcResultMatchers.jsonPath("$.data",
                                                is("ID de tarjeta debe tener exactamente 16 dígitos numéricos")))
                                .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testUpdateBalance_MissingBalance() throws Exception {
                // Given
                String cardId = "1234567890123456"; // 16 dígitos

                // When
                ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/card/balance")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"cardId\":\"" + cardId + "\", \"balance\":\"\"}"));

                // Then
                response.andExpect(status().isBadRequest())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode",
                                                is(HttpStatus.BAD_REQUEST.value())))
                                .andExpect(MockMvcResultMatchers.jsonPath("$.data",
                                                is("Debe insertar el monto de la tarjeta positivo [blanace]")))
                                .andDo(MockMvcResultHandlers.print());
        }
}
