package com.learn.util;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;

@RequiredArgsConstructor
public class VideoProcessingUtil {

    public static void processVideo(Path videoPath, String outputDir360p, String outputDir720p, String outputDir1080p) throws IOException, InterruptedException {
        // FFmpeg commands for different resolutions
        String ffmpegCmd360p = String.format(
                "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -vf scale=640:360 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%03d.ts\" \"%s/playlist.m3u8\"",
                videoPath, outputDir360p, outputDir360p
        );

        String ffmpegCmd720p = String.format(
                "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -vf scale=1280:720 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%03d.ts\" \"%s/playlist.m3u8\"",
                videoPath, outputDir720p, outputDir720p
        );

        String ffmpegCmd1080p = String.format(
                "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -vf scale=1920:1080 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%03d.ts\" \"%s/playlist.m3u8\"",
                videoPath, outputDir1080p, outputDir1080p
        );

        executeFFmpegCommand(ffmpegCmd360p);
        executeFFmpegCommand(ffmpegCmd720p);
        executeFFmpegCommand(ffmpegCmd1080p);
    }

    private static void executeFFmpegCommand(String ffmpegCmd) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", ffmpegCmd);
        processBuilder.inheritIO();
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg command failed with exit code " + exitCode);
        }
    }

}
