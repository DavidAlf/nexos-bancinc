package com.credibanco.bancinc.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Table(name = "CARD")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = true)
    private Customer customer;

    @Column(nullable = false)
    private int numProductCard;

    @Column(nullable = false)
    private long numRandomCard;

    @Column(nullable = false)
    private LocalDate expDate;

    @Column(nullable = false, length = 10)
    private String status;

    @Column(nullable = false)
    private int balance;

    @Transient
    private String cardNumber;

    // Method to update cardNumber based on numProductCard and numRandomCard
    public void updateCardNumber(int numProductCard, long numRandomCard) {
        this.cardNumber = String.format("%06d%010d", numProductCard, numRandomCard);
    }
}
