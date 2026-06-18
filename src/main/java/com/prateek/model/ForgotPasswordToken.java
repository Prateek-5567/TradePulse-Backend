package com.prateek.model;

import com.prateek.domain.VerificationType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ForgotPasswordToken {
    @Id
    private String id;

    @OneToOne
    private User user;   // internally it stores user_id only. ( fKey - pKey concept. )

    private String otp;     // the otp that my code sends to email or mobile will also be stored in this table for verification.

    @Enumerated(EnumType.STRING)
    private VerificationType verificationType;

    private String sendTo;
}
