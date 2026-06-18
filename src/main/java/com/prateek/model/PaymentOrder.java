package com.prateek.model;

import com.prateek.domain.PaymentMethod;
import com.prateek.domain.PaymentOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long amount;

    @Enumerated(EnumType.STRING)
    private PaymentOrderStatus status = PaymentOrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @ManyToOne
    private User user;
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