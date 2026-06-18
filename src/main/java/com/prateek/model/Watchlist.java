package com.prateek.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    private User user;

    @ManyToMany
    private List<Coin> coins = new ArrayList<>();

//      one watchlist has many coins, ( many coins in same list is allowed which is logical also.)
//      AND one coin can appear in many watchlists which is also required.

}

/*
What the data looks like
    Watchlist table
    ────────────────────────
    id  |  user_id ( USER table ki sirf id store hogi reason : JRM java relational Mapping )
    1   |  101        (User A's watchlist)
    2   |  102        (User B's watchlist)

But List<Coin> cannot be stored as a column in that table — a relational DB can't store a list in one cell.
So JPA automatically creates a third join table behind the scenes:

    watchlist_coins (auto created by @ManyToMany)
    ──────────────────────────
    watchlist_id  |  coins_id
    1             |  "bitcoin"
    1             |  "ethereum"
    1             |  "solana"
    2             |  "bitcoin"
    2             |  "dogecoin"

Your understanding mapped out
User A  ──→  Watchlist(id=1)  ──→  [Bitcoin, Ethereum, Solana]
User B  ──→  Watchlist(id=2)  ──→  [Bitcoin, Dogecoin]
One user → one watchlist (@OneToOne) ✅
One watchlist → many coins (@ManyToMany) ✅
Same coin can appear in multiple watchlists ✅

Why @ManyToMany and not @OneToMany?
@OneToMany  → one watchlist has many coins,
              but each coin belongs to only ONE watchlist ❌
              (Bitcoin can only be in User A's watchlist)

@ManyToMany → one watchlist has many coins,
              AND one coin can appear in many watchlists ✅
              (Bitcoin can be in everyone's watchlist)

One thing worth noting — the = new ArrayList<>() default initialization is good practice.
It means watchlist.getCoins() never returns null, it returns an empty list if the user hasn't added any coins yet, avoiding NullPointerException.
 */