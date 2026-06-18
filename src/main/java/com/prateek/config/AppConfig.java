package com.prateek.config;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class AppConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/login/**",
                                "/oauth2/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )

                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(authorization ->
                                authorization.baseUri("/login/oauth2/authorization"))
                        .successHandler((request, response, authentication) -> {

                            // String jwt = JwtProvider.generateToken(authentication); old logic is wrong

                            OAuth2User oauth2User =
                                    (OAuth2User) authentication.getPrincipal();

                            String email =
                                    oauth2User.getAttribute("email");

                            String jwt =
                                    JwtProvider.generateToken(email);

                            // Development Phase
//                            response.sendRedirect(
//                                    "http://localhost:5173/oauth-success?token=" + jwt
//                            );

                            // Production Phase
                            response.sendRedirect(
                                    "http://tradepulse-frontend-pbad.s3-website.ap-south-1.amazonaws.com/oauth-success?token=" + jwt
                            );

                        })
                )

                .addFilterBefore(new JwtTokenValidator(),
                        BasicAuthenticationFilter.class)
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

	    // CORS Configuration
	    private CorsConfigurationSource corsConfigurationSource() {
	        return new CorsConfigurationSource() {
	            @Override
	            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
	                CorsConfiguration cfg = new CorsConfiguration();
	                cfg.setAllowedOrigins(Arrays.asList(
	                    "http://localhost:3000",
	                    "http://localhost:5173",
						"http://localhost:5174",
	                    "http://localhost:4200",
                            "http://tradepulse-frontend-pbad.s3-website.ap-south-1.amazonaws.com"

	                ));
	                cfg.setAllowedMethods(Collections.singletonList("*"));
	                cfg.setAllowCredentials(true);
	                cfg.setAllowedHeaders(Collections.singletonList("*"));
	                cfg.setExposedHeaders(Arrays.asList("Authorization"));
	                cfg.setMaxAge(3600L);
	                return cfg;
	            }
	        };
	    }

	    @Bean
	    PasswordEncoder passwordEncoder() {
			return new BCryptPasswordEncoder();
		}


}

/*
1 HttpServletRequest request
    Definition
    Represents the incoming HTTP request sent by the client (browser/frontend).
    When Google redirects back:
    /login/oauth2/code/google?code=abc123&state=xyz
    This entire data is inside request.

2 HttpServletResponse response
Definition
Represents the HTTP response that will be sent back to the client.

3 DefaultOAuth2User
    Definition
    A Spring Security class representing a user authenticated via OAuth2 provider.
    Purpose
    Stores user details returned by provider (Google).

4. Step-by-Step Execution
        OAuth login succeeds
        Spring creates Authentication object
        onAuthenticationSuccess() is called
        You check:
            instanceof DefaultOAuth2User
        Extract user data from Google
        Create internal User object
 */

/*
OAuth2 vs JWT roles and purpose :-

OAuth2 Authentication: A protocol that delegates user authentication to a third-party provider (e.g., Google).
JWT Authentication: A stateless mechanism where a signed token is used to verify user identity on every request.

OAuth2 → Identity Establishment (Login Phase)
JWT → Identity Verification (Request Phase) ( jwt token is attached with every request along with login requests.)

1 OAuth2 Authentication
Purpose
    To authenticate the user initially using an external provider.
What it does
    Redirects user to Google
    User logs in
    Google verifies credentials
    Google returns user data
    Spring creates Authentication object
Output
    Authenticated User (email, name, etc.)

👉 OAuth2 answers: “Who is the user?”

2 JWT Authentication
Purpose
    To verify the user on every request after login.
What it does
    Client sends JWT in header
    Backend:
    verifies signature
    extracts claims
    Sets authentication in SecurityContext
Output
    Authenticated Request

👉 JWT answers:  “Is this request from a valid user?”


Authentication FLOW :
| Step | Action                                       | Outcome                    |
| ---- | -------------------------------------------- | -------------------------- |
| 1    | User accesses protected url `/api/**'        | Redirected to Google OAuth |
| 2    | Google authentication succeeds               | JWT generated              |
| 3    | Token returned to client                     | Used for future requests   |
| 4    | Client sends `Authorization: Bearer <token>` | Request authenticated      |
| 5    | `JwtTokenValidator` validates token          | Access granted             |

 */