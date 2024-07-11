package com.credibanco.bancinc.service.impl;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.credibanco.bancinc.dto.PurcharseDTO;
import com.credibanco.bancinc.dto.ResponseDTO;
import com.credibanco.bancinc.dto.ResponseErrorDTO;
import com.credibanco.bancinc.dto.TransactionDTO;
import com.credibanco.bancinc.exeptions.ResourceNotFundExcepton;
import com.credibanco.bancinc.model.Card;
import com.credibanco.bancinc.model.Transaction;
import com.credibanco.bancinc.repository.CardRepository;
import com.credibanco.bancinc.repository.TransactionRepository;
import com.credibanco.bancinc.service.TransactionService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CardRepository cardRepository;

    @Override
    public ResponseEntity<ResponseDTO> saveTransaction(PurcharseDTO purchaseDTO) {
        log.info("[TransactionServiceImpl] -> saveTransaction");
        String msnError = "Error en la transaccion";

        try {
            String numProductCard = purchaseDTO.getCardId().substring(0, 6);
            String numRandomCard = purchaseDTO.getCardId().substring(6, 16);

            Optional<Card> cardSaved = cardRepository.findByNumProductCardAndNumRandomCard(
                    Integer.parseInt(numProductCard),
                    Long.parseLong(numRandomCard));

            if (!cardSaved.isPresent()) {
                msnError = "El numero de tarjeta no existe: " + numProductCard + numRandomCard;
                throw new ResourceNotFundExcepton(msnError);
            }

            if (cardSaved.get().getExpDate().isBefore(LocalDate.now())) {
                msnError = "La tarjeta esta vencida: fecha expira[" + cardSaved.get().getExpDate() + "]";
                throw new ResourceNotFundExcepton(msnError);
            }

            if (!cardSaved.get().getStatus().equalsIgnoreCase("enable")) {
                msnError = "La tarjeta tiene estatus invalido: estatus[" + cardSaved.get().getStatus()
                        + "] tiene que estar con estatus [enable]";
                throw new ResourceNotFundExcepton(msnError);
            }

            if (cardSaved.get().getBalance() < purchaseDTO.getPrice()) {
                msnError = "La tarjeta no tiene fondos suficientes: fondo[" + cardSaved.get().getBalance()
                        + "] < precio ["
                        + purchaseDTO.getPrice() + "]";
                throw new ResourceNotFundExcepton(msnError);
            }

            Card cardUpdating = cardSaved.get();
            cardUpdating.setBalance(cardSaved.get().getBalance() - purchaseDTO.getPrice());

            cardRepository.save(cardUpdating);

            Transaction transaction = Transaction.builder()
                    .card(cardUpdating)
                    .expDate(LocalDate.now())
                    .price(purchaseDTO.getPrice())
                    .status("acepted").build();

            transactionRepository.save(transaction);

            TransactionDTO transactionDTO = new TransactionDTO(transaction);

            ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), transactionDTO);

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            log.error("Error saveTransaction " + e.getMessage());
            ResponseErrorDTO responseErrorDTO = new ResponseErrorDTO(HttpStatus.NOT_FOUND.value(),
                    null, msnError);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErrorDTO);
        }
    }

    @Override
    public ResponseEntity<ResponseDTO> getTransactionById(long transactionId) {
        log.info("[TransactionServiceImpl] -> getTransactionById");
        try {
            Optional<Transaction> transaction = transactionRepository.findById(transactionId);

            TransactionDTO transactionDTO = new TransactionDTO(transaction.get());

            ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(), transactionDTO);

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            log.error("Error getTransactionById " + e.getMessage());
            ResponseErrorDTO responseErrorDTO = new ResponseErrorDTO(HttpStatus.NOT_FOUND.value(),
                    null, "Error buscando la transaccion " + transactionId);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErrorDTO);
        }
    }

    @Override
    public ResponseEntity<ResponseDTO> anulationTransaction(PurcharseDTO purchaseDTO) {
        log.info("[TransactionServiceImpl] -> anulationTransaction");
        String msnError = "Error en la transaccion";

        try {
            String numProductCard = purchaseDTO.getCardId().substring(0, 6);
            String numRandomCard = purchaseDTO.getCardId().substring(6, 16);

            Optional<Card> cardSaved = cardRepository.findByNumProductCardAndNumRandomCard(
                    Integer.parseInt(numProductCard),
                    Long.parseLong(numRandomCard));

            if (!cardSaved.isPresent()) {
                msnError = "El numero de tarjeta no existe: " + numProductCard + numRandomCard;
                throw new ResourceNotFundExcepton(msnError);
            }

            Optional<Transaction> transactionSave = transactionRepository
                    .findById(Long.parseLong(purchaseDTO.getTransactionId()));

            if (!transactionSave.isPresent()) {
                msnError = "El numero de transaccion no existe: " + purchaseDTO.getTransactionId();
                throw new ResourceNotFundExcepton(msnError);
            }

            if (transactionSave.get().getStatus().equalsIgnoreCase("annulled")) {
                msnError = "La transaccion ya se encuentra anulada";
                throw new ResourceNotFundExcepton(msnError);
            }

            if (transactionSave.get().getExpDate().isAfter(LocalDate.now().plusDays(1))) {
                msnError = "La transaccion ya no se puede anular por que tiene mas de 24 horas";
                throw new ResourceNotFundExcepton(msnError);
            }

            Card cardUpdating = cardSaved.get();
            cardUpdating.setBalance(cardSaved.get().getBalance() + transactionSave.get().getPrice());

            cardRepository.save(cardUpdating);

            Transaction transactionUpdating = transactionSave.get();
            transactionUpdating.setCard(cardUpdating);
            transactionUpdating.setStatus("annulled");

            transactionRepository.save(transactionUpdating);

            TransactionDTO transactionDTO = new TransactionDTO(transactionUpdating);

            ResponseDTO responseDTO = new ResponseDTO(HttpStatus.OK.value(),
                    transactionDTO);

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            log.error("Error anulationTransaction " + e.getMessage());
            ResponseErrorDTO responseErrorDTO = new ResponseErrorDTO(HttpStatus.NOT_FOUND.value(),
                    null, msnError);

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseErrorDTO);
        }
    }

}
