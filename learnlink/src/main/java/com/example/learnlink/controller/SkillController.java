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

import com.example.learnlink.model.Skill;
import com.example.learnlink.model.User;
import com.example.learnlink.repository.SkillRepository;
import com.example.learnlink.repository.UserRepository;
import com.example.learnlink.service.JwtService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/skills")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class SkillController {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<?> createSkill(@RequestHeader("Authorization") String token,
            @RequestBody Skill skillRequest) {
        String jwt = token.substring(7); // Remove "Bearer "
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        skillRequest.setUser(user);
        Skill savedSkill = skillRepository.save(skillRequest);

        return ResponseEntity.ok(savedSkill);
    }

    @GetMapping
    public ResponseEntity<List<Skill>> getAllSkills() {
        return ResponseEntity.ok(skillRepository.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSkill(@RequestHeader("Authorization") String token, @PathVariable Long id,
            @RequestBody Skill skillRequest) {
        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        if (!skill.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You can only edit your own skills");
        }

        skill.setTitle(skillRequest.getTitle());
        skill.setDescription(skillRequest.getDescription());
        skill.setImageUrl(skillRequest.getImageUrl());

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

}
