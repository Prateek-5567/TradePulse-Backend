package com.prateek.service;


import com.prateek.exception.WalletException;
import com.prateek.model.Order;
import com.prateek.model.User;
import com.prateek.model.Wallet;

public interface WalletService {


    Wallet getUserWallet(User user) throws WalletException;

    public Wallet addBalanceToWallet(Wallet wallet, Long money) throws WalletException;

    public Wallet findWalletById(Long id) throws WalletException;

    public Wallet walletToWalletTransfer(User sender,Wallet receiverWallet, Long amount) throws WalletException;

    public Wallet payOrderPayment(Order order, User user) throws WalletException;



}


/*
when you fetch user from via some service like GET /wallet  from your api-endpoint you get response :

{
    "id": 1,  // ye wallet id hai user "id":2 is mapped with "wallet-id":1
    "user": {
        "id": 2,
        "fullName": "Prateek Bambal",
        "email": "prateekbambal@gmail.com",
        "mobile": "7678352871",
        "status": "PENDING",
        "twoFactorAuth": {
            "sendTo": null,
            "enabled": false
        },
        "picture": null,
        "role": "ROLE_USER",
        "verified": false
    },
    "balance": 0
}

what does these two ids represent ;

1. Inner user.id = 2 → User Entity ID  ( this is user ki ID that differentiate it from all other users)
Meaning
This is the primary key of the User table ;  Represents the actual user in your system
user table looks like :
| id | name    | email |
| -- | ------- | ----- |
| 2  | Prateek | ...   |

2. The outer id =1 is the wallet id
the wallet table looks like :  wallet id = 1 is internally mapped with user_id = 2 so when you fetched wallet info for user id = 2 you get wallet-id=1 vala response.
| id | user_id | balance |
| -- | ------- | ------- |
| 1  | 2       | 0       |

 */