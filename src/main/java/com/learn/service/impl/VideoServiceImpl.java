package com.learn.service.impl;

import com.learn.entity.Video;
import com.learn.repository.VideoRepository;
import com.learn.service.VideoService;
import com.learn.util.FileNameUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoServiceImpl implements VideoService {

    @Value("${files.video}")
    private String DIR;

    @Value("${files.video.hsl}")
    private String HSL_DIR;

    private final VideoRepository videoRepository;
    private final VideoHLS videoHLS;

    @PostConstruct
    public void init() {
        File file = new File(DIR);

        if(!file.exists()) {
            file.mkdir();
        }

        File hsl = new File(DIR);
        if(!hsl.exists()) {
            hsl.mkdir();
        }
    }

    @Override
    public Video save(Video video, MultipartFile file) {

        try {
            String originalFilename = file.getOriginalFilename();
            assert originalFilename != null;
            String uniqueFileName = FileNameUtil.addDateAndUUIDToFileName(originalFilename);
            String contentType = file.getContentType();

            InputStream inputStream = file.getInputStream();

            String cleanedFileName = StringUtils.cleanPath(uniqueFileName);
            String cleanFolder = StringUtils.cleanPath(DIR);

            Path path = Paths.get(cleanFolder, cleanedFileName);
            log.info("Upload Path is {}",  path);

            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

            video.setContentType(contentType);

            // TODO -> Instead of Storing whole path just store cleanedFileName,
            //  and So accordingly Update the Streaming Logic
            video.setFilePath(path.toString());

            Video saved = videoRepository.save(video);

            // processing the Video when the video is uploaded itself
            // TODO -> We can put the event in a Message broker in case there is
            //  an exception while processing the Video to process it again
            videoHLS.processVideo(saved.getVideoId());

            return saved;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Video get(Long videoId) {
        return videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not Found !!!"));
    }

    @Override
    public Video getByTitle(String title) {
        return null;
    }

    @Override
    public List<Video> getAll() {
        return videoRepository.findAll();
    }

    @Override
    public void processVideo(Long videoId) {
        videoHLS.processVideo(videoId);
    }

//    @Override
//    public Long processVideo(Long videoId) {
//        Video video = get(videoId);
//
//        String fileAddress = video.getFilePath();
//
//        Path videoPath = Paths.get(fileAddress);
//
//        String outPut360p = HSL_DIR + videoId + "/360p";
//        String outPut720p = HSL_DIR + videoId + "/720p";
//        String outPut1080p = HSL_DIR + videoId + "/1080";
//
//        try {
////            Files.createDirectories(Paths.get(outPut360p));
////            Files.createDirectories(Paths.get(outPut720p));
////            Files.createDirectories(Paths.get(outPut1080p));
//
//            Path outputPath = Paths.get(HSL_DIR, String.valueOf(videoId));
//            Files.createDirectories(outputPath);
//
//             // Simple Command
//            String ffmpegCmd = String.format(
//                    "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\"  \"%s/master.m3u8\" ",
//                    videoPath, outputPath, outputPath
//            );
//
//            /*
//            StringBuilder ffmpegCmd = new StringBuilder();
//            ffmpegCmd.append("ffmpeg  -i ")
//                    .append(videoPath.toString())
//                    .append(" -c:v libx264 -c:a aac")
//                    .append(" ")
//                    .append("-map 0:v -map 0:a -s:v:0 640x360 -b:v:0 800k ")
//                    .append("-map 0:v -map 0:a -s:v:1 1280x720 -b:v:1 2800k ")
//                    .append("-map 0:v -map 0:a -s:v:2 1920x1080 -b:v:2 5000k ")
//                    .append("-var_stream_map \"v:0,a:0 v:1,a:0 v:2,a:0\" ")
//                    .append("-master_pl_name ").append(HSL_DIR).append(videoId).append("/master.m3u8 ")
//                    .append("-f hls -hls_time 10 -hls_list_size 0 ")
//                    .append("-hls_segment_filename \"").append(HSL_DIR).append(videoId).append("/v%v/fileSequence%d.ts\" ")
//                    .append("\"").append(HSL_DIR).append(videoId).append("/v%v/prog_index.m3u8\"");
//
//            */
//            //file this command
//            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", ffmpegCmd.toString());
//            processBuilder.inheritIO();
//            Process process = processBuilder.start();
//            int exit = process.waitFor();
//            if (exit != 0) {
//                throw new RuntimeException("video processing failed!!");
//            }
//
//            return videoId;
//        } catch (IOException ex) {
//           throw new RuntimeException("Video processing fail!!");
//        } catch (InterruptedException e) {
//           throw new RuntimeException(e);
//        }
//    }

}
