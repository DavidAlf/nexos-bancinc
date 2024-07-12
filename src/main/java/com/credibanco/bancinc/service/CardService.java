package com.credibanco.bancinc.service;

import org.springframework.http.ResponseEntity;

import com.credibanco.bancinc.dto.CardDTO;
import com.credibanco.bancinc.dto.ResponseDTO;

public interface CardService {

    ResponseEntity<ResponseDTO> saveCard(int productId);

    ResponseEntity<ResponseDTO> getCardById(long cardID);

    ResponseEntity<ResponseDTO> enableCard(CardDTO request);

    ResponseEntity<ResponseDTO> deleteCard(String cardNumber);

    ResponseEntity<ResponseDTO> updateBalance(CardDTO request);
}
