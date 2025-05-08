package com.example.learnlink.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Story {

    @Id 
    @GeneratedValue
    private Long id;

    private String mediaUrl;
    private String mediaType; // image/video
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    @ManyToOne
    private User user;
}
