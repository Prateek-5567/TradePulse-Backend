package com.prateek.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class JwtProvider {
	
    private static SecretKey key=Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());
	
	public static String generateToken(Authentication auth) {
		Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
	    String roles = populateAuthorities(authorities);

		String jwt=Jwts.builder()
				.setIssuedAt(new Date())
				.setExpiration(new Date(new Date().getTime()+86400000))
				.claim("email",auth.getName())
				.claim("authorities", roles)
				.signWith(key)
				.compact();
		return jwt;

	}

    public static String generateToken(String email) {

        SecretKey key = Keys.hmacShaKeyFor(
                JwtConstant.SECRET_KEY.getBytes()
        );

        String jwt = Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .claim("email", email)
                .signWith(key)
                .compact();

        return jwt;
    }
	
	public static String getEmailFromJwtToken(String jwt) {
		jwt=jwt.substring(7);
		
		Claims claims=Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt).getBody();
        //above line used to validate a JWT token, decode it, and extract its payload (claims) (claims = playload = object of values ).
        // JWT consists of three parts :  Header.Payload.Signature

		String email=String.valueOf(claims.get("email"));
		
		return email;
	}
	
	public static String populateAuthorities(Collection<? extends GrantedAuthority> collection) {
		Set<String> auths=new HashSet<>();
		
		for(GrantedAuthority authority:collection) {
			auths.add(authority.getAuthority());
		}
		return String.join(",",auths);
	}

}

// Important error That I faced in OAUTH and jwt  :-
/*

| Issue                             | Cause                                            | Resolution                           |
| --------------------------------- | ------------------------------------------------ | ------------------------------------ |
| OAuth session expires immediately | Stateless configuration disables session storage | Generate JWT after OAuth login       |
| Redirect to `/login/google` fails | Custom login page not defined                    | Remove or use default OAuth endpoint |
| Protected APIs return errors      | No JWT provided after OAuth login                | Return JWT to client                 |
| Unauthorized access to `/api/**`  | Missing Bearer token                             | Send JWT in Authorization header     |


5. Authentication Flow After Fix

1. Initiate OAuth Login
    http://localhost:5454/login/oauth2/authorization/google

2. Google Authentication
    User logs in and grants permission.

JWT Generation
Backend generates a JWT and returns it in the response.

Use JWT in Protected APIs

Authorization: Bearer <JWT_TOKEN>

Access Secured Endpoints

POST http://localhost:5454/api/watchlist/create
GET  http://localhost:5454/api/watchlist/user

6. JWT Configuration Validation


Your JWT setup is correct:

Expiration Time: 24 hours
Header: Authorization
Secret Key: Properly configured
Validation Filter: Implemented via JwtTokenValidator.


 */