package com.prateek.controller;

import com.prateek.domain.VerificationType;
import com.prateek.exception.UserException;
import com.prateek.model.ForgotPasswordToken;
import com.prateek.model.User;
import com.prateek.model.VerificationCode;
import com.prateek.request.ResetPasswordRequest;
import com.prateek.request.UpdatePasswordRequest;
import com.prateek.response.ApiResponse;
import com.prateek.response.AuthResponse;
import com.prateek.service.EmailService;
import com.prateek.service.ForgotPasswordService;
import com.prateek.service.UserService;
import com.prateek.service.VerificationService;
import com.prateek.utils.OtpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
public class UserController {
	
	@Autowired
	private UserService userService;

	@Autowired
	private VerificationService verificationService;

	@Autowired
	private ForgotPasswordService forgotPasswordService;

	@Autowired
	private EmailService emailService;


	@GetMapping("/api/users/profile")
	public ResponseEntity<User> getUserProfileHandler(
			@RequestHeader("Authorization") String jwt) throws UserException {

		User user = userService.findUserProfileByJwt(jwt);  // actual flow : fetch email from jwt and then fetch findUserByEmail();
		user.setPassword(null);

		return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
	}
	
	@GetMapping("/api/users/{userId}")
	public ResponseEntity<User> findUserById(
			@PathVariable Long userId,
			@RequestHeader("Authorization") String jwt) throws UserException {

		User user = userService.findUserById(userId);
		user.setPassword(null);

		return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
	}

	@GetMapping("/api/users/email/{email}")
	public ResponseEntity<User> findUserByEmail(
			@PathVariable String email,
			@RequestHeader("Authorization") String jwt) throws UserException {

		User user = userService.findUserByEmail(email);

		return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
	}

	@PatchMapping("/api/users/enable-two-factor/verify-otp/{otp}")
	public ResponseEntity<User> enabledTwoFactorAuthentication(
			@RequestHeader("Authorization") String jwt,
			@PathVariable String otp
	) throws Exception {

		User user = userService.findUserProfileByJwt(jwt);

		VerificationCode verificationCode = verificationService.findUsersVerification(user); // fetches the record of VerificationCode table using user.
        // verificationCode has { id , user,otp , email , mobile , verificationType } fields .
		String sendTo=verificationCode.getVerificationType()
                .equals(VerificationType.EMAIL)?verificationCode.getEmail():verificationCode.getMobile();

		boolean isVerified = verificationService.VerifyOtp(otp, verificationCode);

		if (isVerified) {
			User updatedUser = userService.enabledTwoFactorAuthentication(verificationCode.getVerificationType(),
					sendTo,user);
			verificationService.deleteVerification(verificationCode);
			return ResponseEntity.ok(updatedUser);
		}
		throw new Exception("wrong otp");
	}


    // actually generate and send otp
	@PostMapping("/auth/users/reset-password/send-otp")
	public ResponseEntity<AuthResponse> sendUpdatePasswordOTP(
			@RequestBody UpdatePasswordRequest req)   // contain { sendTo and VerificationType }
			throws Exception {

		User user = userService.findUserByEmail(req.getSendTo());     // SendTo contains email and VerificationType = Email
		String otp= OtpUtils.generateOTP();
		UUID uuid = UUID.randomUUID();
		String id = uuid.toString();

		ForgotPasswordToken token = forgotPasswordService.findByUser(user.getId());    // find my User bhale hi ho internally vo find by user_id hi hota hai...

		if(token==null){
			token=forgotPasswordService.createToken(
					user,id,otp,req.getVerificationType(), req.getSendTo()
			);
		}

		if(req.getVerificationType().equals(VerificationType.EMAIL)){
			emailService.sendVerificationOtpEmail(
					user.getEmail(),
					token.getOtp()
			);
		}

		AuthResponse res=new AuthResponse();
		res.setSession(token.getId());      // we are returning TokenID of ForgetPasswordToken and this token ID will be received by frontend and then again sent to ResetPassword i.e /auth/users/reset-password/verify-otp this api.
		res.setMessage("Password Reset OTP sent successfully.");

		return ResponseEntity.ok(res);

	}

    @PatchMapping("/auth/users/reset-password/verify-otp")
    public ResponseEntity<ApiResponse> resetPassword(
            @RequestParam String id,                            // this id is of ForgotPasswordToken table not the userId.
            @RequestBody ResetPasswordRequest req             // ResetPasswordRequest class contains { password , otp }
    ) throws Exception {

        ForgotPasswordToken forgotPasswordToken=forgotPasswordService.findById(id);

        boolean isVerified = forgotPasswordService.verifyToken(forgotPasswordToken,req.getOtp());

        if (isVerified) {

            userService.updatePassword(forgotPasswordToken.getUser(),req.getPassword());
            ApiResponse apiResponse=new ApiResponse();
            apiResponse.setMessage("password updated successfully");
            return ResponseEntity.ok(apiResponse);
        }
        throw new Exception("wrong otp");

    }

	@PatchMapping("/api/users/verification/verify-otp/{otp}")
	public ResponseEntity<User> verifyOTP(
			@RequestHeader("Authorization") String jwt,
			@PathVariable String otp
	) throws Exception {


		User user = userService.findUserProfileByJwt(jwt);


		VerificationCode verificationCode = verificationService.findUsersVerification(user);


		boolean isVerified = verificationService.VerifyOtp(otp, verificationCode);

		if (isVerified) {
			verificationService.deleteVerification(verificationCode);
			User verifiedUser = userService.verifyUser(user);
			return ResponseEntity.ok(verifiedUser);
		}
		throw new Exception("wrong otp");

	}

    // this OTP is for verification step during SignUp.
	@PostMapping("/api/users/verification/{verificationType}/send-otp")
	public ResponseEntity<String> sendVerificationOTP(
			@PathVariable VerificationType verificationType,
			@RequestHeader("Authorization") String jwt)
            throws Exception {

		User user = userService.findUserProfileByJwt(jwt);

		VerificationCode verificationCode = verificationService.findUsersVerification(user);

		if(verificationCode == null) {
			verificationCode = verificationService.sendVerificationOTP(user,verificationType);
		}


		if(verificationType.equals(VerificationType.EMAIL)){
			emailService.sendVerificationOtpEmail(user.getEmail(), verificationCode.getOtp());
		}

		return ResponseEntity.ok("Verification OTP sent successfully.");

	}

    @PutMapping("/api/users/profile")
    public ResponseEntity<User> updateUserProfile(
            @RequestHeader("Authorization") String jwt,
            @RequestBody User updatedUser
    ) throws UserException {

        User user = userService.findUserProfileByJwt(jwt);

        User savedUser = userService.updateUserProfile(user, updatedUser);

        savedUser.setPassword(null);

        return ResponseEntity.ok(savedUser);
    }

}
