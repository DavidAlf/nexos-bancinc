package com.credibanco.bancinc.service.impl;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.credibanco.bancinc.dto.ResponseDTO;
import com.credibanco.bancinc.dto.ResponseErrorDTO;
import com.credibanco.bancinc.exeptions.ResourceNotFundExcepton;
import com.credibanco.bancinc.model.Card;
import com.credibanco.bancinc.model.Customer;
import com.credibanco.bancinc.repository.CardRepository;
import com.credibanco.bancinc.service.CardService;
import com.credibanco.bancinc.utils.UniqueNumberGenerator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CardServiceImpl implements CardService {

    @Autowired
    private CardRepository cardRepository;

    private UniqueNumberGenerator numRandomCard;

    @Override
    public ResponseEntity<ResponseDTO> saveCard(int numProductCard) {
        log.info("[CardServiceImpl] -> saveCard");

        try {
            numRandomCard = new UniqueNumberGenerator();

            Card card = Card.builder()
                    .numProductCard(numProductCard)
                    .numRandomCard(numRandomCard.generateUniqueNumber())
                    .expDate(LocalDate.now().plusYears(1))
                    .status("disable")
                    .build();

            Optional<Card> cardSaved = cardRepository.findByNumProductCardAndNumRandomCard(card.getNumProductCard(),
                    card.getNumRandomCard());

            if (cardSaved.isPresent()) {
                throw new ResourceNotFundExcepton(
                        "El numero de tarjeta ya existe: " + String.format("%d%d", card.getNumProductCard(),
                                card.getNumRandomCard()));
            }
            cardRepository.save(card);
            Card cardUpdate = card;
            cardUpdate.setCardNumber(String.format("%06d%010d", card.getNumProductCard(), card.getNumRandomCard()));

            ResponseDTO responseDTO = new ResponseDTO(HttpStatus.CREATED.value(), cardUpdate);

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            log.error("Error saveCard " + e.getMessage());
            ResponseErrorDTO responseErrorDTO = new ResponseErrorDTO(HttpStatus.NOT_FOUND.value(),
                    new Customer(), "Error guardando la tarjeta");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErrorDTO);
        }
    }

    @Override
    public ResponseEntity<ResponseDTO> getCardById(long cardID) {
        log.info("[CardServiceImpl] -> getCard");
        try {
            Optional<Card> card = cardRepository.findById(cardID);

            ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), card);

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            log.error("Error getCard " + e.getMessage());
            ResponseErrorDTO responseErrorDTO = new ResponseErrorDTO(HttpStatus.NOT_FOUND.value(),
                    new Customer(), "Error la tarjeta " + cardID);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErrorDTO);
        }
    }

    @Override
    public ResponseEntity<ResponseDTO> enableCard(String cardNumber) {
        log.info("[CardServiceImpl] -> enableCard");
        try {

            String numProductCard = cardNumber.substring(0, 6);
            String numRandomCard = cardNumber.substring(6, 16);

            Optional<Card> cardSaved = cardRepository.findByNumProductCardAndNumRandomCard(
                    Integer.parseInt(numProductCard), Long.parseLong(numRandomCard));

            if (!cardSaved.isPresent()) {
                throw new ResourceNotFundExcepton(
                        "El numero de tarjeta no existe: " + cardNumber);
            }

            Card cardUpdating = cardSaved.get();
            cardUpdating.setStatus("enable");

            Card cardUpdate = cardUpdating;
            cardUpdate.setCardNumber(
                    String.format("%06d%010d", cardUpdating.getNumProductCard(), cardUpdating.getNumRandomCard()));

            cardRepository.save(cardUpdating);

            ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), cardUpdate);

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            log.error("Error enableCard " + e.getMessage());
            ResponseErrorDTO responseErrorDTO = new ResponseErrorDTO(HttpStatus.NOT_FOUND.value(),
                    null, "El numero de tarjeta no existe: " + cardNumber);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErrorDTO);
        }
    }

    @Override
    public ResponseEntity<ResponseDTO> deleteCard(String cardNumber) {
        log.info("[CardServiceImpl] -> deleteCard");
        String msn = "";
        try {
            String numProductCard = cardNumber.substring(0, 6);
            String numRandomCard = cardNumber.substring(6, 16);

            Optional<Card> card = cardRepository.findByNumProductCardAndNumRandomCard(
                    Integer.parseInt(numProductCard), Long.parseLong(numRandomCard));

            if (!card.isPresent()) {
                msn = "El id de la tarjeta no existe: " + cardNumber;
                throw new ResourceNotFundExcepton(msn);
            }

            cardRepository.deleteById(card.get().getId());

            ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), "Eliminado con exito");

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            log.error("Error deleteCard " + e.getMessage());
            ResponseErrorDTO responseErrorDTO = new ResponseErrorDTO(HttpStatus.NOT_FOUND.value(),
                    null, msn);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErrorDTO);
        }
    }

    @Override
    public ResponseEntity<ResponseDTO> updateBalance(String cardNumber, int balance) {
        log.info("[CardServiceImpl] -> updateBalance");
        try {
            String numProductCard = cardNumber.substring(0, 6);
            String numRandomCard = cardNumber.substring(6, 16);

            Optional<Card> cardSaved = cardRepository.findByNumProductCardAndNumRandomCard(
                    Integer.parseInt(numProductCard), Long.parseLong(numRandomCard));

            if (!cardSaved.isPresent()) {
                throw new ResourceNotFundExcepton(
                        "El numero de tarjeta no existe: " + cardNumber);
            }

            Card cardUpdating = cardSaved.get();
            cardUpdating.setBalance(balance);

            Card cardUpdate = cardUpdating;
            cardUpdate.setCardNumber(
                    String.format("%06d%010d", cardUpdating.getNumProductCard(), cardUpdating.getNumRandomCard()));

            cardRepository.save(cardUpdating);

            ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), cardUpdate);

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            log.error("Error updateBalance " + e.getMessage());
            ResponseErrorDTO responseErrorDTO = new ResponseErrorDTO(HttpStatus.NOT_FOUND.value(),
                    null, "El numero de tarjeta no existe: " + cardNumber);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErrorDTO);
        }
    }
}
