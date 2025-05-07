package com.example.learnlink.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.learnlink.model.Goal;

public interface GoalRepository extends JpaRepository<Goal, Long> {

}
