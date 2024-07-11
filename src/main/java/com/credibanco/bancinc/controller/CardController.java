package com.credibanco.bancinc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.credibanco.bancinc.dto.CardDTO;
import com.credibanco.bancinc.dto.ResponseDTO;
import com.credibanco.bancinc.dto.ResponseErrorDTO;
import com.credibanco.bancinc.model.Card;
import com.credibanco.bancinc.service.CardService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/card")
public class CardController {

    @Autowired
    private CardService cardService;

    @SuppressWarnings("null")
    @GetMapping("/{productId}/number")
    public ResponseEntity<ResponseDTO> getCardById(@PathVariable("productId") int productId) {
        log.info("[CardController] -> getCardById [" + productId + "]");

        if (String.valueOf(productId).length() != 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseErrorDTO(HttpStatus.BAD_REQUEST.value(),
                            null, "El número de producto debe tener exactamente 6 dígitos: " + productId));
        }

        ResponseEntity<ResponseDTO> cardDTO = cardService.saveCard(productId);

        if (cardDTO.getStatusCode().equals(HttpStatus.NOT_FOUND) ||
                cardDTO.getBody().getData() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseErrorDTO(HttpStatus.NOT_FOUND.value(),
                            null, "El numero de tarjeta no existe: " + productId));
        }

        Card responseData = (Card) cardDTO.getBody().getData();

        return cardService.getCardById(responseData.getId());
    }

    @PostMapping("/enroll")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ResponseDTO> enableCard(@RequestBody CardDTO cardDTO) {
        log.info("[CardController] -> enableCard [" + cardDTO.getCardId() + "]");

        String cardIdStr = cardDTO.getCardId();
        if (cardIdStr == null || cardIdStr.length() != 16 || !cardIdStr.matches("\\d+")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO(HttpStatus.BAD_REQUEST.value(),
                            "ID de tarjeta debe tener exactamente 16 dígitos numéricos"));
        }

        return cardService.enableCard(cardIdStr);

    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<ResponseDTO> deleteCard(@PathVariable("cardId") String cardId) {
        log.info("[CardController] -> deleteCard [" + cardId + "]");

        return cardService.deleteCard(cardId);
    }

    @PostMapping("/balance")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ResponseDTO> updateBalance(@RequestBody CardDTO cardDTO) {
        log.info("[CardController] -> updateBalance [" + cardDTO.getCardId() + "]");

        String cardIdStr = cardDTO.getCardId();
        if (cardIdStr == null || cardIdStr.length() != 16 || !cardIdStr.matches("\\d+")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO(HttpStatus.BAD_REQUEST.value(),
                            "ID de tarjeta debe tener exactamente 16 dígitos numéricos"));
        }

        if (cardDTO.getBalance() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDTO(HttpStatus.BAD_REQUEST.value(),
                            "Debe insertar el monto de la tarjeta positivo [blanace]"));
        }

        return cardService.updateBalance(cardIdStr, cardDTO.getBalance());
    }
}
