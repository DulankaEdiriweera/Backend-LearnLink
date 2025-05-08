package com.example.learnlink.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.example.learnlink.model.LearningProgress;
import com.example.learnlink.model.ProgressComment;
import com.example.learnlink.model.ProgressCommentRequest;
import com.example.learnlink.model.User;
import com.example.learnlink.repository.LearningProgressRepository;
import com.example.learnlink.repository.ProgressCommentRepository;
import com.example.learnlink.repository.UserRepository;
import com.example.learnlink.service.JwtService;
import org.springframework.web.multipart.MultipartFile;

import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/learning-progress")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class LearningProgressController {

    private final LearningProgressRepository learningProgressRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    
    @Autowired
    private ProgressCommentRepository progressCommentRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createLearningProgress(
            @RequestHeader("Authorization") String token,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("template") String template,
            @RequestParam(value = "media", required = false) MultipartFile media) throws java.io.IOException {

        String jwt = token.substring(7); // Remove "Bearer "
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LearningProgress progress = new LearningProgress();
        progress.setTitle(title);
        progress.setDescription(description);
        progress.setTemplate(template);
        progress.setUser(user);

        if (media != null && !media.isEmpty()) {
            try {
                String uploadDir = "uploads/learning/";
                Files.createDirectories(Paths.get(uploadDir));

                String filePath = uploadDir + media.getOriginalFilename();
                Path path = Paths.get(filePath);
                Files.write(path, media.getBytes());

                progress.setMediaUrl(filePath);

            } catch (IOException e) {
                throw new RuntimeException("Failed to save media file", e);
            }
        }

        LearningProgress savedProgress = learningProgressRepository.save(progress);

        return ResponseEntity.ok(savedProgress);
    }

    @GetMapping
    public ResponseEntity<List<LearningProgress>> getAllLearningProgress() {
        return ResponseEntity.ok(learningProgressRepository.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getLearningProgress(@PathVariable Long id) {
        LearningProgress progress = learningProgressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning progress not found"));
                
        return ResponseEntity.ok(progress);
    }
    
    @GetMapping("/user")
    public ResponseEntity<?> getUserLearningProgress(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        List<LearningProgress> progressList = learningProgressRepository.findByUserOrderByCreatedAtDesc(user);
        
        return ResponseEntity.ok(progressList);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateLearningProgress(
            @RequestHeader("Authorization") String token, 
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("template") String template,
            @RequestParam(value = "media", required = false) MultipartFile media) throws java.io.IOException {
        
        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LearningProgress progress = learningProgressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning progress not found"));

        if (!progress.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You can only edit your own learning progress updates");
        }

        // Set updated fields
        progress.setTitle(title);
        progress.setDescription(description);
        progress.setTemplate(template);

        // If a new media file is provided, save it and update mediaUrl
        if (media != null && !media.isEmpty()) {
            String uploadDir = "uploads/learning/";
            Files.createDirectories(Paths.get(uploadDir));

            String filePath = uploadDir + media.getOriginalFilename();
            Path path = Paths.get(filePath);
            Files.write(path, media.getBytes());

            progress.setMediaUrl(filePath);
        }

        LearningProgress updatedProgress = learningProgressRepository.save(progress);

        return ResponseEntity.ok(updatedProgress);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLearningProgress(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        try {
            String jwt = token.substring(7);
            String email = jwtService.extractUsername(jwt);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            LearningProgress progress = learningProgressRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Learning progress not found"));

            if (!progress.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("You can only delete your own learning progress updates");
            }

            // First delete all comments associated with this learning progress
            List<ProgressComment> comments = progressCommentRepository.findByLearningProgressId(id);
            for (ProgressComment comment : comments) {
                progressCommentRepository.delete(comment);
            }

            // Clear the liked users relationship
            progress.getLikedUsers().clear();
            learningProgressRepository.save(progress);

            // Now delete the learning progress
            learningProgressRepository.delete(progress);

            return ResponseEntity.ok("Learning progress deleted successfully");
        } catch (Exception e) {
            // Add proper error logging
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting learning progress: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<?> likeOrUnlikeLearningProgress(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LearningProgress progress = learningProgressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning progress not found"));

        boolean liked;
        if (progress.getLikedUsers().contains(user)) {
            progress.getLikedUsers().remove(user); // Unlike
            liked = false;
        } else {
            progress.getLikedUsers().add(user); // Like
            liked = true;
        }

        learningProgressRepository.save(progress);

        return ResponseEntity.ok().body(Map.of(
                "liked", liked,
                "likeCount", progress.getLikedUsers().size(),
                "userEmail", user.getEmail()));
    }

    @GetMapping("/{id}/liked-users")
    public ResponseEntity<?> getLikedUsers(@PathVariable Long id) {
        LearningProgress progress = learningProgressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning progress not found"));

        return ResponseEntity.ok(progress.getLikedUsers());
    }
    
    // Comment methods
    
    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody ProgressCommentRequest commentRequest) {
        
        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LearningProgress learningProgress = learningProgressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning progress not found"));

        ProgressComment comment = new ProgressComment();
        comment.setText(commentRequest.getText());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setLearningProgress(learningProgress);
        comment.setUser(user);

        ProgressComment savedComment = progressCommentRepository.save(comment);

        return ResponseEntity.ok(savedComment);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<ProgressComment>> getComments(@PathVariable Long id) {
        List<ProgressComment> comments = progressCommentRepository.findByLearningProgressId(id);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long commentId,
            @RequestBody ProgressCommentRequest updatedRequest) {

        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProgressComment comment = progressCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You can only edit your own comments");
        }

        comment.setText(updatedRequest.getText());
        progressCommentRepository.save(comment);

        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long commentId) {

        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProgressComment comment = progressCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Check if the user is the owner of the comment or the owner of the learning progress
        if (!comment.getUser().getId().equals(user.getId()) && 
            !comment.getLearningProgress().getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You can only delete your own comments or comments on your learning progress");
        }

        progressCommentRepository.delete(comment);
        return ResponseEntity.ok("Comment deleted successfully");
    }
}