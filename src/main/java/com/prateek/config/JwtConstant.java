package com.prateek.config;

public class JwtConstant {
	
	public static final String SECRET_KEY="wpembytrwcvnryxksdbqwjebruyGHyudqgwveytrtrCSnwifoesarjbwe";
	public static final String JWT_HEADER="Authorization";

}
/*
SECRET_KEY
public static final String SECRET_KEY = "...";  this is not jwt token it is just a secret key used for signing ( digital signature )
What it is
A cryptographic key used for:
    Signing JWT (during generation)
    Verifying JWT (during validation)

Purpose
    Ensures:
        Token integrity
        Token authenticity
    What it does
    When JWT is created:
        signature = HMAC(secret_key, header + payload)
    When JWT is validated:
        Same key is used to verify signature

 */

// MUST READ :
// How JWT and OAUTH works :
/*
How the Redirection Works : if user is tries to access authenticated resource or /login he is redirected to http://localhost:5454/login/oauth2/authorization/google ?? HOW

1. User Requests a Protected Resource

    Example:

    http://localhost:5454/api/watchlist/user

    Since your configuration secures /api/**, authentication is required.

    .requestMatchers("/api/**").authenticated()

2. Spring Security Intercepts the Request to authenticated resources .

3. OAuth2 Authorization Begins

The OAuth configuration defines the base authorization URI: ( in AppConfig file )

    authorization.baseUri("/login/oauth2/authorization");

Spring Security automatically appends the provider name (google), producing:

    http://localhost:5454/login/oauth2/authorization/google

This endpoint is internally handled by Spring Security and redirects the user to Google's authentication page.
this way user auto ends up to this redirected uri...
 */
