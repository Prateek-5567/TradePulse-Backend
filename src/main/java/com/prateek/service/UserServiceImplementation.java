package com.prateek.service;

import com.prateek.config.JwtProvider;
import com.prateek.domain.VerificationType;
import com.prateek.exception.UserException;
import com.prateek.model.TwoFactorAuth;
import com.prateek.model.User;
import com.prateek.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserServiceImplementation implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder; // Service interface for encoding passwords. The preferred implementation is BCryptPasswordEncoder.
    // PasswordEncoder is present inside SpringSecurity

    @Autowired
    private EmailService emailService;

	@Override
	public User findUserProfileByJwt(String jwt) throws UserException {

        String email= JwtProvider.getEmailFromJwtToken(jwt);
		
		User user = userRepository.findByEmail(email);
		
		if(user==null) {
			throw new UserException("user not exist with email "+email);
		}
		return user;
	}
	
	@Override
	public User findUserByEmail(String username) throws UserException {
		
		User user=userRepository.findByEmail(username);
		
		if(user!=null) {
			return user;
		}
		
		throw new UserException("user not exist with username "+username); // global exception class ka use hoga
	}

	@Override
	public User findUserById(Long userId) throws UserException {
		Optional<User> opt = userRepository.findById(userId); // findById method exists by default because Id is primary key of User Table.
		
		if(opt.isEmpty()) {
			throw new UserException("user not found with id "+userId);
		}
		return opt.get();  // optional objects ka content is accessed using .get()
	}

	@Override
	public User verifyUser(User user) throws UserException {
		user.setVerified(true); // is the user verified or not .. isVerified is a column in User Table or attibute in User class
		return userRepository.save(user);
	}

	@Override
	public User enabledTwoFactorAuthentication(
			VerificationType verificationType, String sendTo,User user) throws UserException {

        TwoFactorAuth twoFactorAuth=new TwoFactorAuth();    // create a object of TwoFactorAuth class and set input details to it.
		twoFactorAuth.setEnabled(true);
		twoFactorAuth.setSendTo(verificationType);          // EMAIL or MOBILE only two inputs possible from dropbox.

//        if(verificationType == VerificationType.EMAIL) twoFactorAuth.setSendToEmail(sendTo);
//        else if(verificationType == VerificationType.MOBILE) twoFactorAuth.setSendToMobile(sendTo);

		user.setTwoFactorAuth(twoFactorAuth);
		return userRepository.save(user);
	}

	@Override
	public User updatePassword(User user, String newPassword) {
		user.setPassword(passwordEncoder.encode(newPassword));
		return userRepository.save(user);
	}

	@Override
	public void sendUpdatePasswordOtp(String email, String otp) {
        // i think i should change this
	}

    @Override
    public User updateUserProfile(User user, User updatedUser) {

        user.setFullName(updatedUser.getFullName());
        user.setDateOfBirth(updatedUser.getDateOfBirth());
        user.setNationality(updatedUser.getNationality());
        user.setAddress(updatedUser.getAddress());
        user.setCity(updatedUser.getCity());
        user.setPostcode(updatedUser.getPostcode());
        user.setCountry(updatedUser.getCountry());

        return userRepository.save(user);
    }
//
//    @Override
//    public void sendUpdatePasswordOtp(String email, String otp) {
//
//        try {
//            emailService.sendVerificationOtpEmail(email, otp);
//        }
//        catch (Exception e) {
//            throw new RuntimeException("Unable to send password update OTP");
//        }
//
//    }
}
