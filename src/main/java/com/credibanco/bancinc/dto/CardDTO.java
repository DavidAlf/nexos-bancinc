package com.credibanco.bancinc.dto;

import java.time.LocalDate;

import com.credibanco.bancinc.model.Card;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CardDTO {

    private String cardId;
    private LocalDate expDate;
    private String status;
    private int balance;

    public CardDTO(Card card) {
        this.expDate = card.getExpDate();
        this.status = card.getStatus();
        this.balance = card.getBalance();
        this.cardId = String.format("%06d%010d", card.getNumProductCard(), card.getNumRandomCard());
    }

}
