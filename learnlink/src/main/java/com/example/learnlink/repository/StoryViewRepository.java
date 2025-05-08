package com.example.learnlink.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.learnlink.model.Story;
import com.example.learnlink.model.StoryView;
import com.example.learnlink.model.User;

public interface StoryViewRepository extends JpaRepository<StoryView,Long>{

    boolean existsByStoryAndViewer(Story story, User viewer);
    List<StoryView> findByStoryId(Long storyId);
}
