package com.prateek.model;

import com.prateek.domain.VerificationType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class TwoFactorAuth {

    private boolean isEnabled = false;

    @Enumerated(EnumType.STRING)
    private VerificationType sendTo;

//    private String sendToEmail;
//    private String sendToMobile;
}
