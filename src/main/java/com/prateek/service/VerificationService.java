package com.prateek.service;

import com.prateek.domain.VerificationType;
import com.prateek.model.User;
import com.prateek.model.VerificationCode;

public interface VerificationService {
    VerificationCode sendVerificationOTP(User user, VerificationType verificationType);

    VerificationCode findVerificationById(Long id) throws Exception;

    VerificationCode findUsersVerification(User user) throws Exception;

    Boolean VerifyOtp(String opt, VerificationCode verificationCode);

    void deleteVerification(VerificationCode verificationCode);
}

// this verifyOtp works when new user signup to ensure that email or mobile belong to him
// twoFactorOTP vala verifyOTP works when user logs in and user has enabled  twoFactorOTP .