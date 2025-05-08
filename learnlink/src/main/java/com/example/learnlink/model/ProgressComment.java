package com.example.learnlink.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Entity
public class ProgressComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @ManyToOne
    @JoinColumn(name = "learning_progress_id")
    @JsonBackReference
    private LearningProgress learningProgress;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"learningProgress", "comments", "password"})
    private User user;
}