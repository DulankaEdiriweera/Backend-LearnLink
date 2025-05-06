package com.example.learnlink.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "skill_id")
    @JsonBackReference
    private Skill skill;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"skills", "comments", "password"})
    private User user;
}
