package com.prateek.controller;

import com.prateek.domain.WalletTransactionType;
import com.prateek.model.*;
import com.prateek.response.PaymentResponse;
import com.prateek.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController

public class WalletController {

    @Autowired
    private WalletService walleteService;

    @Autowired
    private UserService userService;


    @Autowired
    private OrderService orderService;

    @Autowired
    private WalletTransactionService walletTransactionService;

    @Autowired
    private PaymentService paymentService;


    @GetMapping("/api/wallet")
    public ResponseEntity<?> getUserWallet(@RequestHeader("Authorization")String jwt) throws Exception {
        User user=userService.findUserProfileByJwt(jwt);

        Wallet wallet = walleteService.getUserWallet(user);

        return new ResponseEntity<>(wallet, HttpStatus.OK);
    }

    @GetMapping("/api/wallet/transactions")
    public ResponseEntity<List<WalletTransaction>> getWalletTransaction(
            @RequestHeader("Authorization")String jwt) throws Exception {
        User user=userService.findUserProfileByJwt(jwt);

        Wallet wallet = walleteService.getUserWallet(user);

        List<WalletTransaction> transactions=walletTransactionService.getTransactions(wallet,null);

        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @PutMapping("/api/wallet/deposit/amount/{amount}")
    public ResponseEntity<PaymentResponse> depositMoney(@RequestHeader("Authorization")String jwt,
                                                        @PathVariable Long amount) throws Exception {
        User user =userService.findUserProfileByJwt(jwt);
        Wallet wallet = walleteService.getUserWallet(user);
//        PaymentResponse res = walleteService.depositFunds(user,amount);
        PaymentResponse res = new PaymentResponse();
        res.setPayment_url("deposite success");
        walleteService.addBalanceToWallet(wallet, amount);

        return new ResponseEntity<>(res,HttpStatus.OK);

    }

    @PutMapping("/api/wallet/deposit")
    public ResponseEntity<Wallet> addMoneyToWallet(
            @RequestHeader("Authorization")String jwt,
            @RequestParam(name="order_id") Long orderId,
            @RequestParam(name="payment_id")String paymentId
            ) throws Exception {
        User user =userService.findUserProfileByJwt(jwt);
        Wallet wallet = walleteService.getUserWallet(user);


        PaymentOrder order = paymentService.getPaymentOrderById(orderId);
        Boolean status=paymentService.ProccedPaymentOrder(order,paymentId); //  here it goes.. now the PaymentOrder.status will be marked SUCCESS or FAILURE ( abtak pending hi thaa payment hone ke baad b)
        PaymentResponse res = new PaymentResponse();
        res.setPayment_url("deposite success");

        if(wallet.getBalance()==null) wallet.setBalance(BigDecimal.valueOf(0));  // agar wallet balance null hai to zero set kardo ekbar..

        if(status){
            wallet=walleteService.addBalanceToWallet(wallet, order.getAmount());
        }


        return new ResponseEntity<>(wallet,HttpStatus.OK);

    }

//    @PutMapping("/api/wallet/withdraw/amount/{amount}/user/{userId}")
//    public ResponseEntity<PaymentResponse> withdrawMoney(@PathVariable Long userId, @PathVariable Long amount) throws Exception {
//
//        String wallet = walleteService.depositFunds(userId,amount);
//
//        return new ResponseEntity<>(wallet,HttpStatus.OK);
//    }

    @PutMapping("/api/wallet/{walletId}/transfer")
    public ResponseEntity<Wallet> walletToWalletTransfer(@RequestHeader("Authorization")String jwt,
                                                        @PathVariable Long walletId,
                                                         @RequestBody WalletTransaction req
    ) throws Exception {

        // this method accepts a WalletTransaction type object in Request Body that it will capture

        User senderUser =userService.findUserProfileByJwt(jwt);


        Wallet reciverWallet = walleteService.findWalletById(walletId);

        Wallet wallet = walleteService.walletToWalletTransfer(senderUser,reciverWallet, req.getAmount());
        WalletTransaction walletTransaction=walletTransactionService.createTransaction(
                wallet,
                WalletTransactionType.WALLET_TRANSFER,reciverWallet.getId().toString(),
                req.getPurpose(),
                -req.getAmount()
        );

        return new ResponseEntity<>(wallet,HttpStatus.OK);
// checkout : WalletTransaction under model and walletToWalletTransfer(WalletServiceImplementation) to know more about this function.
    }


    @PutMapping("/api/wallet/order/{orderId}/pay")
    public ResponseEntity<Wallet> payOrderPayment(@PathVariable Long orderId,
                                                  @RequestHeader("Authorization")String jwt) throws Exception {
        User user =userService.findUserProfileByJwt(jwt);
        System.out.println("-------- "+orderId);
        Order order=orderService.getOrderById(orderId);

        Wallet wallet = walleteService.payOrderPayment(order,user);

        return new ResponseEntity<>(wallet,HttpStatus.OK);

    }



}

/*
after completing payment : ( for testing purpose ) (IMP )
essa kuch link milega after making payment bring it and copy here
http://localhost:5173/wallet/?order_id=103&razorpay_payment_id=pay_SbvhiBMSiGlAv2&razorpay_payment_link_id=plink_Sbvh774m9kHmt1&razorpay_payment_link_reference_id=&razorpay_payment_link_status=paid&razorpay_signature=984451f5028ace8d155ab3ccfdefd507128e8738349d4e3407f4c3b2580a4fc7
now focus on orderID and PaymentId  , To proceed the payment you need to deposit the amount
 to your wallet which will call ProceedPaymentOrder() method that make the payment completed and
  marks status = SUCCESS for payment as the payment is still in PENDING state

PUT : /api/wallet/deposit along with request parameters : order_id and payment_id are required. to deposit the payment into wallet
// this step comes after the intermediate state created after making the payment. ; PaymentOrder state
payment remains in pending state only until it is deposited.
 */