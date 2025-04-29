package com.example.learnlink.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.learnlink.model.Skill;

public interface SkillRepository extends JpaRepository<Skill, Long>{

}
