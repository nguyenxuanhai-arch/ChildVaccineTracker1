package com.vaccine.service;

import com.vaccine.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {
    
    User registerUser(String username, String email, String password, String fullName, String phoneNumber, String address);
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    List<User> findAllUsers();
    
    Optional<User> findById(Long id);
    
    User updateUser(User user);
    
    void deleteUser(Long id);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}
