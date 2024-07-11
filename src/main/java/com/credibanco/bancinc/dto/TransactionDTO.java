package com.credibanco.bancinc.dto;

import java.time.LocalDate;

import com.credibanco.bancinc.model.Transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class TransactionDTO {

    private String transactionId;

    private LocalDate expDate;

    private String status;

    private CardDTO cardDTO;

    private int price;

    public TransactionDTO(Transaction transaction) {
        this.transactionId = String.valueOf(transaction.getId());
        this.expDate = transaction.getExpDate();
        this.status = transaction.getStatus();
        this.price = transaction.getPrice();
        this.cardDTO = CardDTO.builder()
                .balance(transaction.getCard().getBalance())
                .cardId(String.format("%d%d", transaction.getCard().getNumProductCard(),
                        transaction.getCard().getNumRandomCard()))
                .expDate(transaction.getCard().getExpDate())
                .status(transaction.getCard().getStatus())
                .build();
    }
}
