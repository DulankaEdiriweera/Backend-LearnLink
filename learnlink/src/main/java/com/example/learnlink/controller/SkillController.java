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
//import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.MediaType;

import com.example.learnlink.model.Skill;
import com.example.learnlink.model.User;
import com.example.learnlink.repository.SkillRepository;
import com.example.learnlink.repository.UserRepository;
import com.example.learnlink.service.JwtService;
import org.springframework.web.multipart.MultipartFile;
import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/skills")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class SkillController {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createSkill(
            @RequestHeader("Authorization") String token,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file) throws java.io.IOException {

        String jwt = token.substring(7); // Remove "Bearer "
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Skill skill = new Skill();
        skill.setTitle(title);
        skill.setDescription(description);
        skill.setUser(user);

        if (file != null && !file.isEmpty()) {
            try {
                String uploadDir = "uploads/";
                Files.createDirectories(Paths.get(uploadDir));

                String filePath = uploadDir + file.getOriginalFilename();
                Path path = Paths.get(filePath);
                Files.write(path, file.getBytes());

                skill.setImageUrl(filePath);

            } catch (IOException e) {
                throw new RuntimeException("Failed to save file", e);
            }
        }

        Skill savedSkill = skillRepository.save(skill);

        return ResponseEntity.ok(savedSkill);
    }

    @GetMapping
    public ResponseEntity<List<Skill>> getAllSkills() {
        return ResponseEntity.ok(skillRepository.findAll());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateSkill(@RequestHeader("Authorization") String token, @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file) throws java.io.IOException {
        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        if (!skill.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You can only edit your own skills");
        }

        // Set updated title and description
        skill.setTitle(title);
        skill.setDescription(description);

        // If a new file is provided, save it and update imageUrl
        if (file != null && !file.isEmpty()) {
            String uploadDir = "uploads/";
            Files.createDirectories(Paths.get(uploadDir));

            String filePath = uploadDir + file.getOriginalFilename();
            Path path = Paths.get(filePath);
            Files.write(path, file.getBytes());

            skill.setImageUrl(filePath);
        }

        Skill updatedSkill = skillRepository.save(skill);

        return ResponseEntity.ok(updatedSkill);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSkill(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        if (!skill.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You can only delete your own skills");
        }

        skillRepository.delete(skill);

        return ResponseEntity.ok("Skill deleted successfully");
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<?> likeOrUnlikeSkill(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        boolean liked;
        if (skill.getLikedUsers().contains(user)) {
            skill.getLikedUsers().remove(user); // Unlike
            liked = false;
        } else {
            skill.getLikedUsers().add(user); // Like
            liked = true;
        }

        skillRepository.save(skill);

        return ResponseEntity.ok().body(Map.of(
                "liked", liked,
                "likeCount", skill.getLikedUsers().size(),
                "userEmail", user.getEmail()));
    }

    @GetMapping("/{id}/liked-users")
    public ResponseEntity<?> getLikedUsers(@PathVariable Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        return ResponseEntity.ok(skill.getLikedUsers());
    }

}
