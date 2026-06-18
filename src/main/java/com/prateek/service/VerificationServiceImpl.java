package com.prateek.service;

import com.prateek.domain.VerificationType;
import com.prateek.model.User;
import com.prateek.model.VerificationCode;
import com.prateek.repository.VerificationRepository;
import com.prateek.utils.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VerificationServiceImpl implements VerificationService{

    @Autowired
    private VerificationRepository verificationRepository;

    @Override
    public VerificationCode sendVerificationOTP(User user, VerificationType verificationType) {

//      VerificationCode{ id , otp , user , email , mobile , verificationType }

        VerificationCode verificationCode = new VerificationCode();

        verificationCode.setOtp(OtpUtils.generateOTP());
        verificationCode.setUser(user);
        verificationCode.setVerificationType(verificationType);

        return verificationRepository.save(verificationCode);
        // you can think email ya mobile to save kia nahi
        // but user to karlia na later jab we actually need to send otp  to we can access verificationCode.getUser().getEmail()
    }

    @Override
    public VerificationCode findVerificationById(Long id) throws Exception {
        // VerificationCode table me given id ke lie koi entry hai to dedo..
        Optional<VerificationCode> verificationCodeOption=verificationRepository.findById(id);
        if(verificationCodeOption.isEmpty()){
            throw new Exception("verification not found");
        }
        return verificationCodeOption.get();
    }

    @Override
    public VerificationCode findUsersVerification(User user) throws Exception {
        return verificationRepository.findByUserId(user.getId());
    }

    @Override
    public Boolean VerifyOtp(String opt, VerificationCode verificationCode) {
//        verificationCode is a object of VerificationCode class what is an entity and stores otp also.
        return opt.equals(verificationCode.getOtp());
    }


    @Override
    public void deleteVerification(VerificationCode verificationCode) {
        // delete a record in VerificationCode table
        verificationRepository.delete(verificationCode);
    }


}
