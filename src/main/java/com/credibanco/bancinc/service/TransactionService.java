package com.credibanco.bancinc.service;

import org.springframework.http.ResponseEntity;

import com.credibanco.bancinc.dto.PurcharseDTO;
import com.credibanco.bancinc.dto.ResponseDTO;

public interface TransactionService {

    ResponseEntity<ResponseDTO> saveTransaction(PurcharseDTO purchaseDTO);

    ResponseEntity<ResponseDTO> getTransactionById(long transactionId);

    ResponseEntity<ResponseDTO> anulationTransaction(PurcharseDTO purchaseDTO);

}
