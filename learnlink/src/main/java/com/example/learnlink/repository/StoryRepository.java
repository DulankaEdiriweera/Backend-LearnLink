package com.example.learnlink.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.learnlink.model.Story;

public interface StoryRepository extends JpaRepository<Story,Long>{

    List<Story> findByExpiresAtAfter(LocalDateTime now);
}
