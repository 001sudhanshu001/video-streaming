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
    private String HLS_DIR;

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

}
