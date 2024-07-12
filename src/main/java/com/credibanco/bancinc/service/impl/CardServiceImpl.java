package com.credibanco.bancinc.service.impl;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.credibanco.bancinc.dto.CardDTO;
import com.credibanco.bancinc.dto.ResponseDTO;
import com.credibanco.bancinc.dto.ResponseErrorDTO;
import com.credibanco.bancinc.model.Card;
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
    public ResponseEntity<ResponseDTO> saveCard(int productId) {
        log.info("[CardServiceImpl] -> saveCard");
        String msnError = "Error en el consumo saveCard";
        HttpStatus codeError = HttpStatus.NOT_FOUND;

        try {
            numRandomCard = new UniqueNumberGenerator();

            if (String.valueOf(productId).length() != 6) {
                msnError = "El número de producto debe tener exactamente 6 dígitos: " + productId;
                codeError = HttpStatus.BAD_REQUEST;

                return ResponseEntity.status(codeError)
                        .body(new ResponseErrorDTO(codeError.value(), null, msnError));
            }

            Card card = Card.builder()
                    .numProductCard(productId)
                    .numRandomCard(numRandomCard.generateUniqueNumber())
                    .expDate(LocalDate.now().plusYears(1))
                    .status("disable")
                    .build();

            Optional<Card> cardSaved = cardRepository.findByNumProductCardAndNumRandomCard(card.getNumProductCard(),
                    card.getNumRandomCard());

            if (cardSaved.isPresent()) {
                codeError = HttpStatus.NOT_ACCEPTABLE;
                msnError = "El numero de tarjeta ya existe: " + String.format("%d%d", card.getNumProductCard(),
                        card.getNumRandomCard());
                // throw new ResourceNotFundExcepton(msnError);
                return ResponseEntity.status(codeError).body(new ResponseErrorDTO(codeError.value(), null, msnError));
            }
            cardRepository.save(card);

            ResponseDTO responseDTO = new ResponseDTO(HttpStatus.CREATED.value(), card);

            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);

        } catch (Exception e) {
            log.error("Error saveCard " + e.getMessage());
            ResponseErrorDTO responseErrorDTO = new ResponseErrorDTO(codeError.value(), null,
                    "Error guardando la tarjeta");

            return ResponseEntity.status(codeError).body(responseErrorDTO);
        }
    }

    @Override
    public ResponseEntity<ResponseDTO> getCardById(long cardID) {
        log.info("[CardServiceImpl] -> getCard");

        try {
            Optional<Card> card = cardRepository.findById(cardID);
            CardDTO cardDTO = new CardDTO(card.get());

            ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), cardDTO);

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            log.error("Error getCard " + e.getMessage());
            ResponseErrorDTO responseErrorDTO = new ResponseErrorDTO(HttpStatus.NOT_FOUND.value(),
                    null, "Error la tarjeta " + cardID);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErrorDTO);
        }
    }

    @Override
    public ResponseEntity<ResponseDTO> enableCard(CardDTO request) {
        log.info("[CardServiceImpl] -> enableCard");
        String msnError = "Error en el consumo enableCard";
        HttpStatus codeError = HttpStatus.NOT_FOUND;
        try {

            if (request.getCardId() == null || request.getCardId().length() != 16
                    || !request.getCardId().matches("\\d+")) {
                msnError = "ID de tarjeta debe tener exactamente 16 dígitos numéricos";
                codeError = HttpStatus.BAD_REQUEST;

                return ResponseEntity.status(codeError).body(new ResponseErrorDTO(codeError.value(), null, msnError));
            }

            String numProductCard = request.getCardId().substring(0, 6);
            String numRandomCard = request.getCardId().substring(6, 16);

            Optional<Card> cardSaved = cardRepository.findByNumProductCardAndNumRandomCard(
                    Integer.parseInt(numProductCard), Long.parseLong(numRandomCard));

            if (!cardSaved.isPresent()) {
                msnError = "El numero de tarjeta no existe: " + request.getCardId();
                codeError = HttpStatus.NOT_FOUND;

                return ResponseEntity.status(codeError).body(new ResponseErrorDTO(codeError.value(), null, msnError));
            }

            Card cardUpdating = cardSaved.get();
            cardUpdating.setStatus("enable");

            CardDTO cardDTO = new CardDTO(cardUpdating);

            cardRepository.save(cardUpdating);

            ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), cardDTO);

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            log.error("Error enableCard " + e.getMessage());
            ResponseErrorDTO responseErrorDTO = new ResponseErrorDTO(codeError.value(), null, msnError);

            return ResponseEntity.status(codeError).body(responseErrorDTO);
        }
    }

    @Override
    public ResponseEntity<ResponseDTO> deleteCard(String cardNumber) {
        log.info("[CardServiceImpl] -> deleteCard");
        String msnError = "Error en el consumo deleteCard";
        HttpStatus codeError = HttpStatus.NOT_FOUND;
        try {

            if (cardNumber == null || cardNumber.length() != 16 || !cardNumber.matches("\\d+")) {
                msnError = "ID de tarjeta debe tener exactamente 16 dígitos numéricos";
                codeError = HttpStatus.BAD_REQUEST;

                return ResponseEntity.status(codeError).body(new ResponseErrorDTO(codeError.value(), null, msnError));
            }

            String numProductCard = cardNumber.substring(0, 6);
            String numRandomCard = cardNumber.substring(6, 16);

            Optional<Card> card = cardRepository.findByNumProductCardAndNumRandomCard(
                    Integer.parseInt(numProductCard), Long.parseLong(numRandomCard));

            if (!card.isPresent()) {
                msnError = "El numero de la tarjeta no existe: " + cardNumber;
                codeError = HttpStatus.NOT_FOUND;

                return ResponseEntity.status(codeError).body(new ResponseErrorDTO(codeError.value(), null, msnError));
            }

            cardRepository.deleteById(card.get().getId());

            ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), "Eliminado con exito");

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            log.error("Error deleteCard " + e.getMessage());
            ResponseErrorDTO responseErrorDTO = new ResponseErrorDTO(codeError.value(), null, msnError);

            return ResponseEntity.status(codeError).body(responseErrorDTO);
        }
    }

    @Override
    public ResponseEntity<ResponseDTO> updateBalance(CardDTO request) {
        log.info("[CardServiceImpl] -> updateBalance");
        String msnError = "Error en el consumo updateBalance";
        HttpStatus codeError = HttpStatus.NOT_FOUND;

        try {

            if (request.getCardId() == null || request.getCardId().length() != 16
                    || !request.getCardId().matches("\\d+")) {
                msnError = "ID de tarjeta debe tener exactamente 16 dígitos numéricos";
                codeError = HttpStatus.BAD_REQUEST;

                return ResponseEntity.status(codeError).body(new ResponseErrorDTO(codeError.value(), null, msnError));
            }

            if (request.getBalance() <= 0) {
                msnError = "Debe insertar el monto de la tarjeta positivo [blanace]";
                codeError = HttpStatus.BAD_REQUEST;

                return ResponseEntity.status(codeError).body(new ResponseErrorDTO(codeError.value(), null, msnError));
            }

            String numProductCard = request.getCardId().substring(0, 6);
            String numRandomCard = request.getCardId().substring(6, 16);

            Optional<Card> cardSaved = cardRepository.findByNumProductCardAndNumRandomCard(
                    Integer.parseInt(numProductCard), Long.parseLong(numRandomCard));

            if (!cardSaved.isPresent()) {
                msnError = "El numero de tarjeta no existe: " + request.getCardId();
                codeError = HttpStatus.NOT_FOUND;

                return ResponseEntity.status(codeError).body(new ResponseErrorDTO(codeError.value(), null, msnError));
            }

            Card cardUpdating = cardSaved.get();
            cardUpdating.setBalance(request.getBalance());

            CardDTO cardDTO = new CardDTO(cardUpdating);
            cardRepository.save(cardUpdating);

            ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), cardDTO);

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            log.error("Error updateBalance " + e.getMessage());
            ResponseErrorDTO responseErrorDTO = new ResponseErrorDTO(codeError.value(), null, msnError);

            return ResponseEntity.status(codeError).body(responseErrorDTO);
        }
    }

}
