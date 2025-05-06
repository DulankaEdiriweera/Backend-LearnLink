package com.example.learnlink.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.learnlink.model.User;
import com.example.learnlink.repository.UserRepository;
import com.example.learnlink.service.JwtService;
import com.example.learnlink.service.UserService;

import io.jsonwebtoken.io.IOException;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;


    @GetMapping("/me")
    public ResponseEntity<?> getLoggedInUser(@RequestHeader("Authorization") String token) {

        String jwt = token.substring(7);// Remove "Bearer " prefix
        String email = jwtService.extractUsername(jwt); // Extract email from token

        Optional<User> userOptional = userService.findByEmail(email);

        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @PostMapping(value = { "/update" }, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String token,
            @RequestParam("username") String username,
            @RequestParam("handle") String handle,
            @RequestParam("bio") String bio,
            @RequestParam("work") String work,
            @RequestParam("studied") String studied,
            @RequestParam(value = "profilePic", required = false) MultipartFile profilePic,
            @RequestParam(value = "backgroundImg", required = false) MultipartFile backgroundImg)
            throws java.io.IOException {

        String jwt = token.substring(7); // Remove "Bearer "
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update the user details
    user.setUsername(username);
    user.setHandle(handle);
    user.setBio(bio);
    user.setWork(work);
    user.setStudied(studied);
    
        // Check and save profile picture
        if (profilePic != null && !profilePic.isEmpty()) {
            try {
                String uploadDir = "uploads/";
                Files.createDirectories(Paths.get(uploadDir));

                String fileName = System.currentTimeMillis() + "_" + profilePic.getOriginalFilename();
                Path filePath = Paths.get(uploadDir + fileName);
                Files.write(filePath, profilePic.getBytes());

                user.setProfilePic(filePath.toString()); // Store path to profile picture

            } catch (IOException e) {
                throw new RuntimeException("Failed to save profile picture", e);
            }
        }

        // Check and save background image
        if (backgroundImg != null && !backgroundImg.isEmpty()) {
            try {
                String uploadDir = "uploads/";
                Files.createDirectories(Paths.get(uploadDir));

                String fileName = System.currentTimeMillis() + "_" + backgroundImg.getOriginalFilename();
                Path filePath = Paths.get(uploadDir + fileName);
                Files.write(filePath, backgroundImg.getBytes());

                user.setBackgroundImg(filePath.toString()); // Store path to background image

            } catch (IOException e) {
                throw new RuntimeException("Failed to save background image", e);
            }
        }

        // Save the updated user
        User updatedUser = userRepository.save(user);

        return ResponseEntity.ok(updatedUser);
    }

}
