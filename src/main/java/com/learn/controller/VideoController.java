package com.learn.controller;

import com.learn.dto.response.CustomMessage;
import com.learn.entity.Video;
import com.learn.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @PostMapping
    public ResponseEntity<?> save(
            @RequestParam("file")MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description
    ) {

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);

        Video savedVideo = videoService.save(video, file);

        if(savedVideo != null) {
            return ResponseEntity.status(HttpStatus.OK).body(video);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomMessage.builder()
                        .message("Something went Wrong !!!")
                        .success(false)
                        .build()
                );

    }
}
