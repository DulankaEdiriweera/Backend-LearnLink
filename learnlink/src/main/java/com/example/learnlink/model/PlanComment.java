package com.example.learnlink.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Entity
public class PlanComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "learning_plan_id")
    @JsonBackReference
    private LearningPlan learningPlan;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({ "learning-plans", "comments", "password" })
    private User user;
}
