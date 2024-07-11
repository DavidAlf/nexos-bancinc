package com.credibanco.bancinc.service;

import org.springframework.http.ResponseEntity;

import com.credibanco.bancinc.dto.ResponseDTO;

public interface CardService {

    ResponseEntity<ResponseDTO> saveCard(int numProductCard);

    ResponseEntity<ResponseDTO> getCardById(long cardID);

    ResponseEntity<ResponseDTO> enableCard(String cardNumber);

    ResponseEntity<ResponseDTO> deleteCard(String cardNumber);

    ResponseEntity<ResponseDTO> updateBalance(String cardNumber, int balance);
}
