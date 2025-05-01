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

    public String followOrUnfollowUser(Long targetUserId, String token) {
        String email = jwtService.extractEmail(token);
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        if (targetUser.getFollowers().contains(currentUser)) {
            // Unfollow
            targetUser.getFollowers().remove(currentUser);
            currentUser.getFollowing().remove(targetUser);
            userRepository.save(targetUser);
            userRepository.save(currentUser);
            return "Unfollowed successfully";
        } else {
            // Follow
            targetUser.getFollowers().add(currentUser);
            currentUser.getFollowing().add(targetUser);
            userRepository.save(targetUser);
            userRepository.save(currentUser);
            return "Followed successfully";
        }
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
}

