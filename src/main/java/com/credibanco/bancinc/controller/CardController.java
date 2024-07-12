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

        ResponseEntity<ResponseDTO> cardDTO = cardService.saveCard(productId);

        if (!cardDTO.getStatusCode().equals(HttpStatus.CREATED)) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(new ResponseErrorDTO(HttpStatus.NOT_ACCEPTABLE.value(),
                            null, "El numero de tarjeta no se creo: " + productId));
        }

        Card responseData = (Card) cardDTO.getBody().getData();

        return cardService.getCardById(responseData.getId());
    }

    @PostMapping("/enroll")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<ResponseDTO> enableCard(@RequestBody CardDTO request) {
        log.info("[CardController] -> enableCard [" + request.getCardId() + "]");

        return cardService.enableCard(request);

    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<ResponseDTO> deleteCard(@PathVariable("cardId") String cardId) {
        log.info("[CardController] -> deleteCard [" + cardId + "]");

        return cardService.deleteCard(cardId);
    }

    @PostMapping("/balance")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<ResponseDTO> updateBalance(@RequestBody CardDTO request) {
        log.info("[CardController] -> updateBalance [" + request.getCardId() + "]");

        return cardService.updateBalance(request);
    }
}
