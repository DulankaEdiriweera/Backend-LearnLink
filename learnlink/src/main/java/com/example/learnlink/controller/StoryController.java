package com.example.learnlink.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.learnlink.dto.StoryDTO;
import com.example.learnlink.model.Skill;
import com.example.learnlink.model.Story;
import com.example.learnlink.model.StoryView;
import com.example.learnlink.model.User;
import com.example.learnlink.repository.StoryRepository;
import com.example.learnlink.repository.StoryViewRepository;
import com.example.learnlink.repository.UserRepository;
import com.example.learnlink.service.JwtService;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/stories")
public class StoryController {

    @Autowired
    private StoryRepository storyRepo;
    @Autowired
    private StoryViewRepository viewRepo;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadStory(
            @RequestHeader("Authorization") String token,
            @RequestParam("mediaType") String mediaType,
            @RequestParam(value = "file", required = false) MultipartFile file) throws java.io.IOException {

        String jwt = token.substring(7); // Remove "Bearer "
        String email = jwtService.extractUsername(jwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Story story = new Story();
        story.setUser(user);
        story.setMediaType(mediaType);
        story.setCreatedAt(LocalDateTime.now());
        story.setExpiresAt(LocalDateTime.now().plusHours(24));

        if (file != null && !file.isEmpty()) {
            String uploadDir = "uploads/";
            Files.createDirectories(Paths.get(uploadDir));

            String filePath = uploadDir + file.getOriginalFilename();
            Path path = Paths.get(filePath);
            Files.write(path, file.getBytes());

            story.setMediaUrl(filePath);
        }

        return ResponseEntity.ok(storyRepo.save(story));
    }

    @GetMapping
    public List<StoryDTO> getActiveStories() {
        return storyRepo.findByExpiresAtAfter(LocalDateTime.now())
                .stream()
                .map(s -> {
                    StoryDTO dto = new StoryDTO();
                    dto.setId(s.getId());
                    dto.setMediaUrl(s.getMediaUrl());
                    dto.setMediaType(s.getMediaType());
                    dto.setCreatedAt(s.getCreatedAt());
                    dto.setUsername(s.getUser().getUsername());
                    dto.setProfilePicUrl(s.getUser().getProfilePic());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<?> viewStory(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String email = jwtService.extractUsername(jwt);
        User viewer = userRepository.findByEmail(email).orElseThrow();

        Story story = storyRepo.findById(id).orElseThrow();

        if (!viewRepo.existsByStoryAndViewer(story, viewer)) {
            StoryView view = new StoryView();
            view.setStory(story);
            view.setViewer(viewer);
            view.setViewedAt(LocalDateTime.now());
            viewRepo.save(view);
        }

        return ResponseEntity.ok().build();
    }

    // @GetMapping("/{id}/views")
    // public List<String> getStoryViews(@PathVariable Long id) {
    // return viewRepo.findByStoryId(id)
    // .stream()
    // .map(v -> v.getViewer().getProfilePic())

    // .collect(Collectors.toList());
    // }

    @GetMapping("/{id}/views")
    public List<Map<String, String>> getStoryViews(@PathVariable Long id) {
        return viewRepo.findByStoryId(id)
                .stream()
                .map(v -> {
                    Map<String, String> viewerDetails = new HashMap<>();
                    viewerDetails.put("username", v.getViewer().getUsername());
                    viewerDetails.put("profilePic", v.getViewer().getProfilePic());
                    return viewerDetails;
                })
                .collect(Collectors.toList());
    }

}
