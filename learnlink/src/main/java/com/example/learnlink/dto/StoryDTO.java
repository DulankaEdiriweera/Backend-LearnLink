package com.example.learnlink.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class StoryDTO {

    private Long id;
    private String mediaUrl;
    private String mediaType;
    private LocalDateTime createdAt;
    private String username;
    private String profilePicUrl;
}
