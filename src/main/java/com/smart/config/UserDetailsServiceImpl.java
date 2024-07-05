package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smart.dao.UserRespository;
import com.smart.entities.User;

public class UserDetailsServiceImpl implements UserDetailsService{

	
	@Autowired
	private UserRespository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	    // Fetching user from the database
	    User user = userRepository.getUserByUserName(username);
	    
	    // If user is not found, throw UsernameNotFoundException
	    if(user == null) {
	        throw new UsernameNotFoundException("User not found with username: " + username);
	    }
	    
	    // Create CustomUserDetails object using the retrieved user
	    CustomUserDetails customUserDetails = new CustomUserDetails(user);
	    
	    return customUserDetails;
	}


}
