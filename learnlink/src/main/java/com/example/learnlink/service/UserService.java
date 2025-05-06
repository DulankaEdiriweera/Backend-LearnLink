package com.example.learnlink.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.learnlink.model.User;
import com.example.learnlink.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;



    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
}

