package com.credibanco.bancinc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.credibanco.bancinc.dto.PurcharseDTO;
import com.credibanco.bancinc.dto.ResponseDTO;
import com.credibanco.bancinc.service.TransactionService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/purchase")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ResponseDTO> saveTransaction(@RequestBody @Validated PurcharseDTO purchaseDTO) {
        log.info("[TransactionController] -> saveTransaction");

        return transactionService.saveTransaction(purchaseDTO);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ResponseDTO> getTransactionById(@PathVariable("transactionId") Long transactionId) {
        log.info("[TransactionController] -> getCustomerByEmail [" + transactionId + "]");

        return transactionService.getTransactionById(transactionId);
    }

    @PostMapping("/anulation")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ResponseDTO> anulationTransaction(@RequestBody @Validated PurcharseDTO purchaseDTO) {
        log.info("[TransactionController] -> anulationTransaction");

        return transactionService.anulationTransaction(purchaseDTO);
    }

}
