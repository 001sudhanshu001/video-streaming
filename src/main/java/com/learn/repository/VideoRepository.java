package com.learn.repository;

import com.learn.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, String> {
    Optional<Video> findByTitle(String title);
}
