package com.prateek.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "wallets")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne       // ← tells ORM: "this is a relationship"
    private User user;      // ← Java sees a full User object and joins via Primary Key of USER table. ( Join PrimaryKey se hi hoga INternally)
//    Tum wallet.user.email likh sakte ho directly — ORM ne pehle se JOIN karke object ready kar diya hota hai.

    private BigDecimal balance = BigDecimal.ZERO;
}
