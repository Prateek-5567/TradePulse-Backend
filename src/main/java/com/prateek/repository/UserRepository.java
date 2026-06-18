package com.prateek.repository;



import com.prateek.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


// Repository of User class .
public interface UserRepository extends JpaRepository<User, Long> {
	
	public User findByEmail(String email);

}
