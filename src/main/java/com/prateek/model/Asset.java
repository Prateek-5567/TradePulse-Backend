package com.prateek.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Asset { // store assets of users.
// Asset represent ki konse user ne konse coin ki kintni quantity hold kari hui hai and at what price.
//    now , there will be many many entries in asset table
//    multiple assets can belong to same coin.
//    multiple assets can belong to same user.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double quantity;
    private double buyPrice;

    @ManyToOne
    private Coin coin;
//    Multiple rows in Assets can refer to same row in Coin table because There can be Multiple assests for same Coin.

    @ManyToOne
    private User user;
//    Multiple rows in Assets can refer to same row in Users table because There can be Multiple assests for same user.

}
