package com.prateek.controller;

import com.razorpay.RazorpayException;
import com.stripe.exception.StripeException;
import com.prateek.domain.PaymentMethod;
import com.prateek.exception.UserException;
import com.prateek.model.PaymentOrder;
import com.prateek.model.User;
import com.prateek.response.PaymentResponse;
import com.prateek.service.PaymentService;
import com.prateek.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentService paymentService;


    @PostMapping("/api/payment/{paymentMethod}/amount/{amount}")
    public ResponseEntity<PaymentResponse> paymentHandler(
            @PathVariable PaymentMethod paymentMethod,
            @PathVariable Long amount,
            @RequestHeader("Authorization") String jwt) throws UserException, RazorpayException, StripeException {

        User user = userService.findUserProfileByJwt(jwt);

        PaymentResponse paymentResponse;

        PaymentOrder order= paymentService.createOrder(user, amount,paymentMethod);

        if(paymentMethod.equals(PaymentMethod.RAZORPAY)){
            paymentResponse=paymentService.createRazorpayPaymentLink(user,amount,
                    order.getId());
        }
        else{
            paymentResponse=paymentService.createStripePaymentLink(user,amount, order.getId());
        }

        return new ResponseEntity<>(paymentResponse, HttpStatus.CREATED);
    }


}

// How does payment flow :

/*

User initiates payment
       ↓
PaymentOrder created (holds amount, userId, paymentMethod) → status: PENDING
       ↓
Razorpay/Stripe processes payment → returns payment_id + order_id
       ↓
Client calls your callback/deposit endpoint with payment_id + order_id
       ↓
You call `ProccedPaymentOrder()` to verify payment → status: SUCCESS
       ↓
Only then → credit the wallet

 */