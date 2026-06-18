package com.prateek.service;

import com.razorpay.Payment;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.prateek.domain.PaymentMethod;
import com.prateek.domain.PaymentOrderStatus;
import com.prateek.model.PaymentOrder;
import com.prateek.model.User;
import com.prateek.repository.PaymentOrderRepository;
import com.prateek.response.PaymentResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService{

    @Value("${stripe.api.key}")
    private String stripeSecretKey;

    @Value("${razorpay.api.key}")
    private String apiKey;

    @Value("${razorpay.api.secret}")
    private String apiSecret;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;


    @Override
    public PaymentOrder createOrder(User user, Long amount, PaymentMethod paymentMethod) {
        PaymentOrder order=new PaymentOrder();
        order.setUser(user);
        order.setAmount(amount);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(PaymentOrderStatus.PENDING); // set status to pending initially why ? (checkout the flow of payment)
        return paymentOrderRepository.save(order);
    }

    @Override
    public PaymentOrder getPaymentOrderById(Long id) throws Exception {
        Optional<PaymentOrder> optionalPaymentOrder=paymentOrderRepository.findById(id);
        if(optionalPaymentOrder.isEmpty()){
            throw new Exception("payment order not found with id "+id);
        }
        return optionalPaymentOrder.get();
    }

    @Override
    public Boolean ProccedPaymentOrder(PaymentOrder paymentOrder, String paymentId)
            throws RazorpayException {

        // Checkout WalletController-addMoneyToWallet  now..

        if (!paymentOrder.getStatus().equals(PaymentOrderStatus.PENDING)) {
            // if paymentOrder is not processed yet i.e its Status == PENDING then only we move ahead else we return this func. with a false.
            // the logic means if status==PENDING is not true then return false ; because further processing is for pending paymentOrders ,
            // because you need to do something about that order .. to make it either success or failed.
            return false;
        }

        boolean isSuccess = true;

        if (paymentOrder.getPaymentMethod().equals(PaymentMethod.RAZORPAY)) {
            RazorpayClient razorpay = new RazorpayClient(apiKey, apiSecret);
            Payment payment = razorpay.payments.fetch(paymentId);
            String status = payment.get("status");

            isSuccess = "captured".equals(status);
        }

        paymentOrder.setStatus(
                isSuccess ? PaymentOrderStatus.SUCCESS : PaymentOrderStatus.FAILED
        );

        paymentOrderRepository.save(paymentOrder);
        return isSuccess;
    }

    @Override
    public PaymentResponse createRazorpayPaymentLink(User user,
                                                     Long Amount,
                                                     Long orderId)
            throws RazorpayException {

        Long amount = Amount * 100;


        try {
            // Instantiate a Razorpay client with your key ID and secret
            RazorpayClient razorpay = new RazorpayClient(apiKey, apiSecret);

            JSONObject paymentLinkRequest = new JSONObject();
            paymentLinkRequest.put("amount",amount);
            paymentLinkRequest.put("currency","INR");


            // Create a JSON object with the customer details
            JSONObject customer = new JSONObject();
            customer.put("name",user.getFullName());

            customer.put("email",user.getEmail());
            paymentLinkRequest.put("customer",customer);

            // Create a JSON object with the notification settings
            JSONObject notify = new JSONObject();
            notify.put("email",true);
            paymentLinkRequest.put("notify",notify);

            // Set the reminder settings
            paymentLinkRequest.put("reminder_enable",true);

            // Set the callback URL and method
            paymentLinkRequest.put("callback_url","http://localhost:5173/wallet/?order_id="+orderId);
//          We just sent the order_id not the payment_id but to ProccedPaymentOrder() both are required
//          Razorpay automatically appends razorpay_payment_id and razorpay_payment_link_id as query parameters to your callback URL.
//          So the frontend actually receives: all three and uses whatever required.
            paymentLinkRequest.put("callback_method","get");

            // Create the payment link using the paymentLink.create() method
            PaymentLink payment = razorpay.paymentLink.create(paymentLinkRequest);

            String paymentLinkId = payment.get("id");
            String paymentLinkUrl = payment.get("short_url");

            PaymentResponse res=new PaymentResponse();
            res.setPayment_url(paymentLinkUrl);


            return res;

        } catch (RazorpayException e) {

            System.out.println("Error creating payment link: " + e.getMessage());
            throw new RazorpayException(e.getMessage());
        }
    }

    @Override
    public PaymentResponse createStripePaymentLink(User user, Long amount,Long orderId) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:5173/wallet?order_id="+orderId) // frontend will be at 5173 localhost ( you need to update this url after hosting it )
                .setCancelUrl("http://localhost:5173/payment/cancel")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(amount*100)
                                .setProductData(SessionCreateParams
                                        .LineItem
                                        .PriceData
                                        .ProductData
                                        .builder()
                                        .setName("Top up wallet")
                                        .build()
                                ).build()
                        ).build()
                ).build();

        Session session = Session.create(params);

        System.out.println("session _____ " + session);

        PaymentResponse res = new PaymentResponse();
        res.setPayment_url(session.getUrl());

        return res;
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
}
