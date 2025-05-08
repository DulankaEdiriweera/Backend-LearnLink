package com.example.learnlink.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.learnlink.model.ProgressComment;

import java.util.List;

public interface ProgressCommentRepository extends JpaRepository<ProgressComment, Long> {
    List<ProgressComment> findByLearningProgressId(Long learningProgressId);
}