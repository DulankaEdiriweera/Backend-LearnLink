package com.example.learnlink.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import com.example.learnlink.model.LearningPlan;
import com.example.learnlink.model.PlanComment;
import com.example.learnlink.model.PlanCommentRequest;
import com.example.learnlink.model.User;
import com.example.learnlink.repository.LearningPlanRepository;
import com.example.learnlink.repository.PlanCommentRepository;
import com.example.learnlink.repository.UserRepository;
import com.example.learnlink.service.JwtService;
import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/learning-plans")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class LearningPlanController {

    private final LearningPlanRepository learningPlanRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createLearningPlan(
            @RequestHeader("Authorization") String token,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "file", required = false) MultipartFile file)
            throws java.io.IOException, ParseException {

        // Check for missing parameters
        if (title == null || description == null || startDate == null || endDate == null) {
            return ResponseEntity.badRequest().body("Missing required parameters");
        }

        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date parsedStartDate = formatter.parse(startDate);
        Date parsedEndDate = formatter.parse(endDate);

        LearningPlan learningPlan = new LearningPlan();
        learningPlan.setTitle(title);
        learningPlan.setDescription(description);
        learningPlan.setStartDate(parsedStartDate);
        learningPlan.setEndDate(parsedEndDate);
        learningPlan.setUser(user);

        // If a file is provided, save it
        if (file != null && !file.isEmpty()) {
            try {
                String uploadDir = "uploads/";
                Files.createDirectories(Paths.get(uploadDir));

                String filePath = uploadDir + file.getOriginalFilename();
                Path path = Paths.get(filePath);
                Files.write(path, file.getBytes());

                learningPlan.setImageUrl(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save file", e);
            }
        }

        LearningPlan savedLearningPlan = learningPlanRepository.save(learningPlan);
        return ResponseEntity.ok(savedLearningPlan);
    }

    @GetMapping
    public ResponseEntity<List<LearningPlan>> getAllLearningPlans() {
        return ResponseEntity.ok(learningPlanRepository.findAll());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateLearningPlan(@RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "file", required = false) MultipartFile file)
            throws java.io.IOException, ParseException {

        // Check for missing parameters
        if (title == null || description == null || startDate == null || endDate == null) {
            return ResponseEntity.badRequest().body("Missing required parameters");
        }

        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LearningPlan learningPlan = learningPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning Plan not found"));

        if (!learningPlan.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You can only edit your own learning plans");
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date parsedStartDate = formatter.parse(startDate);
        Date parsedEndDate = formatter.parse(endDate);

        learningPlan.setTitle(title);
        learningPlan.setDescription(description);
        learningPlan.setStartDate(parsedStartDate);
        learningPlan.setEndDate(parsedEndDate);
        learningPlan.setUser(user);

        // If a new file is provided, save it and update imageUrl
        if (file != null && !file.isEmpty()) {
            String uploadDir = "uploads/";
            Files.createDirectories(Paths.get(uploadDir));

            String filePath = uploadDir + file.getOriginalFilename();
            Path path = Paths.get(filePath);
            Files.write(path, file.getBytes());

            learningPlan.setImageUrl(filePath);
        }

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

    // Like or Unlike a Learning Plan
    @PutMapping("/{id}/like")
    public ResponseEntity<?> likeOrUnlikeLearningPlan(@RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LearningPlan learningPlan = learningPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning Plan not found"));

        boolean liked;
        if (learningPlan.getLikedUsers().contains(user)) {
            learningPlan.getLikedUsers().remove(user); // Unlike
            liked = false;
        } else {
            learningPlan.getLikedUsers().add(user); // Like
            liked = true;
        }

        learningPlanRepository.save(learningPlan);

        return ResponseEntity.ok().body(Map.of(
                "liked", liked,
                "likeCount", learningPlan.getLikedUsers().size(),
                "userEmail", user.getEmail()));
    }

    // Get all users who liked a Learning Plan
    @GetMapping("/{id}/liked-users")
    public ResponseEntity<?> getLikedUsers(@PathVariable Long id) {
        LearningPlan learningPlan = learningPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning Plan not found"));

        return ResponseEntity.ok(learningPlan.getLikedUsers());
    }

    @Autowired
    private PlanCommentRepository planCommentRepository;

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(@RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody PlanCommentRequest commentRequest) {
        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LearningPlan learningPlan = learningPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning Plan not found"));

        PlanComment comment = new PlanComment();
        comment.setText(commentRequest.getText());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setLearningPlan(learningPlan);
        comment.setUser(user);

        PlanComment savedComment = planCommentRepository.save(comment);

        return ResponseEntity.ok(savedComment);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<PlanComment>> getComments(@PathVariable Long id) {
        List<PlanComment> comments = planCommentRepository.findByLearningPlanId(id);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(@RequestHeader("Authorization") String token,
            @PathVariable Long commentId,
            @RequestBody PlanCommentRequest updatedRequest) {

        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PlanComment comment = planCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You can only edit your own comments");
        }

        comment.setText(updatedRequest.getText());
        planCommentRepository.save(comment);

        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@RequestHeader("Authorization") String token,
            @PathVariable Long commentId) {

        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PlanComment comment = planCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You can only delete your own comments");
        }

        planCommentRepository.delete(comment);
        return ResponseEntity.ok("Comment deleted successfully");
    }

}
