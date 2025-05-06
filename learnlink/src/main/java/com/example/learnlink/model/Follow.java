package com.example.learnlink.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The follower
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id")
    @JsonIgnoreProperties({"password"})
    private User follower;

    // The user being followed
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id")
    @JsonIgnoreProperties({"password"})
    private User following;
}
