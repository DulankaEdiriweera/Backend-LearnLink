package com.example.learnlink.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.learnlink.model.Follow;

public interface FollowRepository extends JpaRepository<Follow,Long>{

    Follow findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
