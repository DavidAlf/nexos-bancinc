package com.credibanco.bancinc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.credibanco.bancinc.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

}
