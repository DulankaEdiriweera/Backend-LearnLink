package com.example.learnlink.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.learnlink.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository <Comment, Long> {
    List<Comment> findBySkillId(Long skillId);
}
