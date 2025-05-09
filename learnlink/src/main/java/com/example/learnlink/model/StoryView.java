package com.example.learnlink.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class StoryView {

     @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Story story;

    @ManyToOne
    private User viewer;

    private LocalDateTime viewedAt;

}
