package com.credibanco.bancinc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class PurcharseDTO {

    private String transactionId;
    private String cardId;
    private int price;
}
