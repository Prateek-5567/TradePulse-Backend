package com.prateek.model;

import com.prateek.domain.OrderStatus;
import com.prateek.domain.OrderType;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor        // generate constructor will all fields - object initialisation
@NoArgsConstructor      // generate constructor with no fields ( default constructor of empty object creation )
@Entity
@Table(name = "orders")
public class Order {    // Stores A buy or sell request placed by a user. ( order history table )

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;  // JRM - JAVA relational Mapping handles Foreignkey to PKey Mapping auto.
                        // Mapping ke lie column annotation is not required,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;


    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private OrderItem orderItem;        // Mapping ke lie column annotation is not required,


}
