package com.prateek.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
	
	private String jwt;
	private boolean status;
	private String message;
	private boolean isTwoFactorAuthEnabled=false;  // by default false.
	private String session;

}
