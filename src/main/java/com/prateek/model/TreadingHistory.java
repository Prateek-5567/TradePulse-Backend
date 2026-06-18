package com.prateek.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TreadingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private double sellingPrice;

    private double buyingPrice;

    @Embedded
    private Coin coin;  // tells which coin?

    @ManyToOne
    private User user; // multiple trading history rows for same user.
}
