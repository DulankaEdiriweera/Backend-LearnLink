package com.example.learnlink.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.MediaType;

import com.example.learnlink.model.Goal;
import com.example.learnlink.model.GoalComment; // **Added GoalComment import**: This is needed to use GoalComment in the controller.
import com.example.learnlink.model.GoalCommentRequest; // **Change: This should match the correct request model for comments.**
import com.example.learnlink.model.User;
import com.example.learnlink.repository.GoalRepository;
import com.example.learnlink.repository.UserRepository;
import com.example.learnlink.repository.GoalCommentRepository; // **Added GoalCommentRepository import**: For handling comments.
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

    // **Inject GoalCommentRepository** (missing previously)
    private final GoalCommentRepository goalCommentRepository;

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

        // 1. Clear liked users (ManyToMany)
        goal.getLikedUsers().clear();
        goalRepository.save(goal); // save after clearing

        // 2. Delete comments manually
        goalCommentRepository.deleteAll(goalCommentRepository.findByGoalId(goal.getId()));

        // 3. Delete image file if exists
        if (goal.getImageUrl() != null && !goal.getImageUrl().isEmpty()) {
            try {
                Path path = Paths.get(goal.getImageUrl());
                Files.deleteIfExists(path);
            } catch (Exception e) {
                System.err.println("Could not delete file: " + e.getMessage());
            }
        }

        // 4. Delete goal
        goalRepository.delete(goal);

        return ResponseEntity.ok("Goal deleted successfully");
    }

    // Like or Unlike Goal - **Corrected**
    @PutMapping("/{id}/like")
    public ResponseEntity<?> likeOrUnlikeGoal(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String jwt = token.substring(7); // Remove "Bearer "
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

    // Get All Users Who Liked a Goal - **Corrected**
    @GetMapping("/{id}/liked-users")
    public ResponseEntity<?> getLikedUsers(@PathVariable Long id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        // Return the list of users who liked this goal
        return ResponseEntity.ok(goal.getLikedUsers());
    }

    // **New Section for Goal Commenting**

    // Add Comment to Goal - **ADDED**
    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(@RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody GoalCommentRequest commentRequest) { // **Accepting comment as GoalCommentRequest**
        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        GoalComment comment = new GoalComment(); // **Creating GoalComment instance**
        comment.setText(commentRequest.getText());
        comment.setCreatedAt(LocalDateTime.now()); // **Set current timestamp**
        comment.setGoal(goal); // **Set associated goal**
        comment.setUser(user);

        GoalComment savedComment = goalCommentRepository.save(comment); // **Save comment using GoalCommentRepository**

        return ResponseEntity.ok(savedComment);
    }

    // Get Comments for a Goal - **ADDED**
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<GoalComment>> getComments(@PathVariable Long id) {
        List<GoalComment> comments = goalCommentRepository.findByGoalId(id); // **Fetch comments for specific goal**
        return ResponseEntity.ok(comments);
    }

    // Update Comment - **ADDED**
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(@RequestHeader("Authorization") String token,
            @PathVariable Long commentId,
            @RequestBody GoalCommentRequest updatedRequest) {

        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GoalComment comment = goalCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You can only edit your own comments");
        }

        comment.setText(updatedRequest.getText());
        goalCommentRepository.save(comment);

        return ResponseEntity.ok(comment);
    }

    // Delete Comment - **ADDED**
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@RequestHeader("Authorization") String token,
            @PathVariable Long commentId) {

        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GoalComment comment = goalCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You can only delete your own comments");
        }

        goalCommentRepository.delete(comment); // **Delete comment**

        return ResponseEntity.ok("Comment deleted successfully");
    }

}
