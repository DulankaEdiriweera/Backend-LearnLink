package com.example.learnlink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.learnlink.model.GoalComment;

import java.util.List;

public interface GoalCommentRepository extends JpaRepository<GoalComment, Long> {
    List<GoalComment> findByGoalId(Long goalId);
}
