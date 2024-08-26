package com.learn.service.impl;

import com.learn.entity.Video;
import com.learn.repository.VideoRepository;
import com.learn.util.VideoProcessingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class VideoHLS {

    @Value("${files.video.hsl}")
    private String HLS_DIR;

    private final VideoRepository videoRepository;

    @Async
    public void processVideo(Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not Found !!!"));
        String fileAddress = video.getFilePath();
        Path videoPath = Paths.get(fileAddress);

        // Output directories for different resolutions
        String outputDir360p = HLS_DIR + videoId + "/360p";
        String outputDir720p = HLS_DIR + videoId + "/720p";
        String outputDir1080p = HLS_DIR + videoId + "/1080p";

        try {
            // Create directories for each resolution
            Files.createDirectories(Paths.get(outputDir360p));
            Files.createDirectories(Paths.get(outputDir720p));
            Files.createDirectories(Paths.get(outputDir1080p));

            // FFmpeg commands for different resolutions
            VideoProcessingUtil.processVideo(videoPath, outputDir360p, outputDir720p, outputDir1080p);

        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException("Video processing failed!", ex);
        }
    }
}
