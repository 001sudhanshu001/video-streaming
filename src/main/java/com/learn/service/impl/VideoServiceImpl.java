package com.learn.service.impl;

import com.learn.entity.Video;
import com.learn.repository.VideoRepository;
import com.learn.service.VideoService;
import com.learn.util.FileNameUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.spi.EvictEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoServiceImpl implements VideoService {

    @Value("${files.video}")
    private String DIR;

    private final VideoRepository videoRepository;

    @PostConstruct
    public void init() {
        File file = new File(DIR);

        if(!file.exists()) {
            file.mkdir();
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
            video.setFilePath(path.toString());

            return videoRepository.save(video);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Video get(String videoId) {
        return null;
    }

    @Override
    public Video getByTitle(String title) {
        return null;
    }

    @Override
    public List<Video> getAll() {
        return null;
    }

}
