package com.example.learnlink.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.MediaType;

import com.example.learnlink.model.Goal;
import com.example.learnlink.model.User;
import com.example.learnlink.repository.GoalRepository;
import com.example.learnlink.repository.UserRepository;
import com.example.learnlink.service.JwtService;
import org.springframework.web.multipart.MultipartFile;
import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/Goals")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class GoalController {
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createGoal(
            @RequestHeader("Authorization") String token,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("startDate") String startDate,
            @RequestParam("dueDate") String dueDate,
            @RequestParam(value = "file", required = false) MultipartFile file) throws java.io.IOException {

        String jwt = token.substring(7); // Remove "Bearer "
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Goal goal = new Goal();
        goal.setTitle(title);
        goal.setDescription(description);
        goal.setStartDate(startDate);
        goal.setDueDate(dueDate);
        goal.setUser(user);

        if (file != null && !file.isEmpty()) {
            try {
                String uploadDir = "uploads/";
                Files.createDirectories(Paths.get(uploadDir));

                String filePath = uploadDir + file.getOriginalFilename();
                Path path = Paths.get(filePath);
                Files.write(path, file.getBytes());

                goal.setImageUrl(filePath);

            } catch (IOException e) {
                throw new RuntimeException("Failed to save file", e);
            }
        }

        Goal savedGoal = goalRepository.save(goal);

        return ResponseEntity.ok(savedGoal);
    }

    @GetMapping
    public ResponseEntity<List<Goal>> getAllGoals() {
        return ResponseEntity.ok(goalRepository.findAll());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateGoal(@RequestHeader("Authorization") String token, @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("startDate") String startDate,
            @RequestParam("dueDate") String dueDate,
            @RequestParam(value = "file", required = false) MultipartFile file) throws java.io.IOException {
        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        if (!goal.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You can only edit your own goal");
        }

        goal.setTitle(title);
        goal.setDescription(description);
        goal.setStartDate(startDate);
        goal.setDueDate(dueDate);

        if (file != null && !file.isEmpty()) {
            String uploadDir = "uploads/";
            Files.createDirectories(Paths.get(uploadDir));

            String filePath = uploadDir + file.getOriginalFilename();
            Path path = Paths.get(filePath);
            Files.write(path, file.getBytes());

            goal.setImageUrl(filePath);
        }

        Goal updatedGoal = goalRepository.save(goal);

        return ResponseEntity.ok(updatedGoal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        if (!goal.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You can only delete your own goals");
        }

        goalRepository.delete(goal);

        return ResponseEntity.ok("Goal deleted successfully");
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<?> likeOrUnlikeGoal(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        boolean liked;
        if (goal.getLikedUsers().contains(user)) {
            goal.getLikedUsers().remove(user); // Unlike
            liked = false;
        } else {
            goal.getLikedUsers().add(user); // Like
            liked = true;
        }

        goalRepository.save(goal);

        return ResponseEntity.ok().body(Map.of(
                "liked", liked,
                "likeCount", goal.getLikedUsers().size(),
                "userEmail", user.getEmail()));
    }

    @GetMapping("/{id}/liked-users")
    public ResponseEntity<?> getLikedUsers(@PathVariable Long id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        return ResponseEntity.ok(goal.getLikedUsers());
    }
}
