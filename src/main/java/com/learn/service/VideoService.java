package com.learn.service;

import com.learn.entity.Video;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {
    Video save(Video video, MultipartFile file);

    Video get(Long videoId);

    Video getByTitle(String title);

    List<Video> getAll();

    void processVideo(Long videoId);

}
