package com.prateek.model;

import com.prateek.domain.WalletTransactionType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    private WalletTransactionType type;

    private LocalDate date;

    private String transferId;

    private String purpose;

    private Long amount;


}


/*
this is used in Request body to make Wallet Transactions

Required:

amount — without this the transfer can't happen

Optional (set by the backend, don't send in request body):

id — auto generated for each transaction and each transaction is stored in a database table to keep track so there must be a @Id
wallet — Wallet Object set by backend from JWT of current user.
type — hardcoded as WALLET_TRANSFER in controller
purpose - optional
date — should be set by backend (check your service, you may need to add walletTransaction.setDate(LocalDate.now()))
transferId — set by backend as receiverWalletId of current logged in user ya kahe browser ke pass jis user ka jwt hai currently
// Browser ke pass hamara jwt hota hai that the service provider gives it temporarily so that I can be authenticated and access apis for me..
So the only fields you need to send are amount and purpose.
 */