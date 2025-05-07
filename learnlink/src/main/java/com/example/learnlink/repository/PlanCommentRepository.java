package com.example.learnlink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.learnlink.model.PlanComment;

import java.util.List;

public interface PlanCommentRepository extends JpaRepository<PlanComment, Long> {
    List<PlanComment> findByLearningPlanId(Long learningPlanId);
}
