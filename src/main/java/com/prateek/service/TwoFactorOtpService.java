package com.prateek.service;

import com.prateek.model.TwoFactorOTP;
import com.prateek.model.User;

public interface TwoFactorOtpService {

    // TwoFactorOTP is a table inside model ; it stores { id , otp , user , jwt }

    TwoFactorOTP createTwoFactorOtpRecordInTable(User user, String otp, String jwt);

    TwoFactorOTP findByUserId(Long userId);

    TwoFactorOTP findById(String id);

    boolean verifyTwoFactorOtp(TwoFactorOTP twoFactorOtp,String otp);

    void deleteTwoFactorOtp(TwoFactorOTP twoFactorOTP);

}
