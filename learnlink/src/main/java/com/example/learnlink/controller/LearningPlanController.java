package com.example.learnlink.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.learnlink.model.LearningPlan;
import com.example.learnlink.model.User;
import com.example.learnlink.repository.LearningPlanRepository;
import com.example.learnlink.repository.UserRepository;
import com.example.learnlink.service.JwtService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/learning-plans")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class LearningPlanController {

    private final LearningPlanRepository learningPlanRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<?> createLearningPlan(@RequestHeader("Authorization") String token,
            @RequestBody LearningPlan learningPlanRequest) {
        String jwt = token.substring(7); // Remove "Bearer "
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        learningPlanRequest.setUser(user);
        LearningPlan savedLearningPlan = learningPlanRepository.save(learningPlanRequest);

        return ResponseEntity.ok(savedLearningPlan);
    }

    @GetMapping
    public ResponseEntity<List<LearningPlan>> getAllLearningPlans() {
        return ResponseEntity.ok(learningPlanRepository.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLearningPlan(@RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody LearningPlan learningPlanRequest) {
        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LearningPlan learningPlan = learningPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning Plan not found"));

        if (!learningPlan.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You can only edit your own learning plans");
        }

        learningPlan.setTitle(learningPlanRequest.getTitle());
        learningPlan.setDescription(learningPlanRequest.getDescription());
        learningPlan.setImageUrl(learningPlanRequest.getImageUrl());
        learningPlan.setStartDate(learningPlanRequest.getStartDate());
        learningPlan.setEndDate(learningPlanRequest.getEndDate());

        LearningPlan updatedLearningPlan = learningPlanRepository.save(learningPlan);

        return ResponseEntity.ok(updatedLearningPlan);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLearningPlan(@RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LearningPlan learningPlan = learningPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning Plan not found"));

        if (!learningPlan.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You can only delete your own learning plans");
        }

        learningPlanRepository.delete(learningPlan);

        return ResponseEntity.ok("Learning Plan deleted successfully");
    }
}
