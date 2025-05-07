package com.example.learnlink.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.learnlink.model.LearningProgress;
import com.example.learnlink.model.User;

import java.util.List;

public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {
    List<LearningProgress> findByUserOrderByCreatedAtDesc(User user);
}