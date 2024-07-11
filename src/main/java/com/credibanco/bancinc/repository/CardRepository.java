package com.credibanco.bancinc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.credibanco.bancinc.model.Card;

public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByNumProductCardAndNumRandomCard(@Param(("numProductCard")) int numProductCard,
            @Param(("numRandomCard")) long numRandomCard);

    List<Card> findByCustomerId(@Param(("customerID")) long customerID);
}
