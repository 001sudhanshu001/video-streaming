package com.learn.service.impl;

import com.learn.entity.Video;
import com.learn.service.VideoService;
import com.learn.util.FileNameUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class VideoServiceImpl implements VideoService {

    @Value("${files.video}")
    private static String DIR;
    @Override
    public Video save(Video video, MultipartFile file) {
        return null;
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
