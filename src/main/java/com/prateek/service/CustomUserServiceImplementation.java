package com.prateek.service;

import com.prateek.model.User;
import com.prateek.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;



@Service
public class CustomUserServiceImplementation implements UserDetailsService {

    private UserRepository userRepository;    // this is called dependency injection , we inject what is required here.
	
	public CustomUserServiceImplementation(UserRepository userRepository) {
		this.userRepository=userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User user = userRepository.findByEmail(username);
		
		if(user==null) {
			throw new UsernameNotFoundException("user not found with email - "+username); // refers to @ControllerAdvice class - Global exception handler.
		}
		
		List<GrantedAuthority> authorities=new ArrayList<>(); // ROLE : USER / ADMIN

		return new org.springframework.security.core.userdetails.User(
				user.getEmail(),user.getPassword(),authorities
        );
	}

}
