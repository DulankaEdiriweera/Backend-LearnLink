package com.example.learnlink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.learnlink.model.LearningPlan;

public interface LearningPlanRepository extends JpaRepository<LearningPlan, Long> {

}
