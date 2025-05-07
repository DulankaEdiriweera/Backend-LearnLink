package com.example.learnlink.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    private String username;
    private String email;
    private String password;

    private String provider; // "local" or "google"
    private String handle;
    private String bio;
    private String work;
    private String studied;
    private String profilePic;
    private String backgroundImg;

    @OneToMany(mappedBy = "follower")
    @JsonIgnore
    private List<Follow> followingUsers = new ArrayList<>();

    @OneToMany(mappedBy = "following")
    @JsonIgnore
    private List<Follow> followerUsers = new ArrayList<>();

}
