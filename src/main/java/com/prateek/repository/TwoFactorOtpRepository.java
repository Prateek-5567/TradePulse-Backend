package com.prateek.repository;

import com.prateek.model.TwoFactorOTP;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TwoFactorOtpRepository extends JpaRepository<TwoFactorOTP,String> {

    TwoFactorOTP findByUserId(Long userId); // by default sirf findByID hota hai and here Id means TwoFactorOTP ki primary key vali id
    // UserId is a attribute in TwoFactorOTP class soo... we can have findByUserId() method .
}
