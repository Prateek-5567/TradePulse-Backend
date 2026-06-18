package com.prateek.service;


import com.prateek.domain.VerificationType;
import com.prateek.exception.UserException;
import com.prateek.model.User;


public interface UserService {

	public User findUserProfileByJwt(String jwt) throws UserException;
	
	public User findUserByEmail(String email) throws UserException;
	
	public User findUserById(Long userId) throws UserException;

	public User verifyUser(User user) throws UserException;

	public User enabledTwoFactorAuthentication(VerificationType verificationType,
											   String sendTo, User user) throws UserException;

	User updatePassword(User user, String newPassword);

	void sendUpdatePasswordOtp(String email,String otp);

    User updateUserProfile(
            User user,
            User updatedUser
    );

//	void sendPasswordResetEmail(User user);
}
