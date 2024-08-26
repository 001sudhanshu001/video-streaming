package com.learn.controller;

import com.learn.dto.response.CustomMessage;
import com.learn.dto.response.ResponseDto;
import com.learn.entity.Video;
import com.learn.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoController {

    @Value("${files.video.hsl}")
    private String HLS_DIR;

    private static final List<String> ALLOWED_RESOLUTIONS = Arrays.asList("360p", "720p", "1080p");
    private static final String DEFAULT_RESOLUTION = "360p";

    private final VideoService videoService;

    @PostMapping
    public ResponseEntity<?> save(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description
    ) {

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);

        Video savedVideo = videoService.save(video, file);

        if (savedVideo != null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseDto.builder()
                            .video(savedVideo)
                            .message("Video is Being Processed for multiple Resolutions, It may take some time")
                            .build());
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomMessage.builder()
                        .message("Something went Wrong !!!")
                        .success(false)
                        .build()
                );

    }

    // Without Range, Sending in Chunks
    @GetMapping("/stream/s/{videoId}")
    public ResponseEntity<StreamingResponseBody> streaming(@PathVariable Long videoId,
                                                           @RequestHeader HttpHeaders headers)
                                                           throws IOException {

        Video video = videoService.get(videoId);
        String contentType = video.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        String filePath = video.getFilePath();
        StreamingResponseBody stream = outputStream -> {
            try (InputStream inputStream = new FileInputStream(filePath)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    System.out.println("Streaming");
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(stream);
    }


    @GetMapping("/stream/range/{videoId}")
    public ResponseEntity<Resource> streamVideoRange(@PathVariable Long videoId,
                                                     @RequestHeader(value = "Range", required = false) String range) {
        System.out.println(range);
        //

        Video video = videoService.get(videoId);
        Path path = Paths.get(video.getFilePath());

        Resource resource = new FileSystemResource(path);

        String contentType = video.getContentType();

        if (contentType == null) {
            contentType = "application/octet-stream";

        }
        long fileLength = path.toFile().length();

        if (range == null) {
            System.out.println("RETURING OLD WAY");
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
        }

        System.out.println("GOING NEW WAY");

        long rangeStart;

        long rangeEnd;

        String[] ranges = range.replace("bytes=", "").split("-");
        rangeStart = Long.parseLong(ranges[0]);

        rangeEnd = rangeStart + 1024 * 1024 - 1;

        if (rangeEnd >= fileLength) {
            rangeEnd = fileLength - 1;
        }

//        if (ranges.length > 1) {
//            rangeEnd = Long.parseLong(ranges[1]);
//        } else {
//            rangeEnd = fileLength - 1;
//        }
//
//        if (rangeEnd > fileLength - 1) {
//            rangeEnd = fileLength - 1;
//        }


//        System.out.println("range start : " + rangeStart);
//        System.out.println("range end : " + rangeEnd);
        InputStream inputStream;

        try {

            inputStream = Files.newInputStream(path);
            inputStream.skip(rangeStart);
            long contentLength = rangeEnd - rangeStart + 1;


            byte[] data = new byte[(int) contentLength];
            int read = inputStream.read(data, 0, data.length);
            System.out.println("read(number of bytes) : " + read);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("X-Content-Type-Options", "nosniff");
            headers.setContentLength(contentLength);

            return ResponseEntity
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new ByteArrayResource(data));


        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }


    }


    @GetMapping
    public List<Video> getAll() {
        return videoService.getAll();
    }

   // @GetMapping("/process/{videoId}")
    public String processVideo(@PathVariable Long videoId) {
        videoService.processVideo(videoId);

        return "The Files are being Processed";
    }

    @GetMapping("/{videoId}/master.m2u8") // This API will be used to stream the segments
    public ResponseEntity<Resource> serverMasterFile(
            @PathVariable String videoId,
            @RequestHeader(value = "Accept-Resolution", defaultValue = DEFAULT_RESOLUTION) String resolution) {
        if (!ALLOWED_RESOLUTIONS.contains(resolution)) {
            resolution = DEFAULT_RESOLUTION;
        }

        Path path = Paths.get(HLS_DIR, videoId, resolution, "playlist.m3u8");
        System.out.println("PATH IS " + path);

        if(!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Resource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl"
                )
                .body(resource);

    }

    @GetMapping("/{videoId}/{segment}.ts")
    public ResponseEntity<Resource> serveSegments(@PathVariable String videoId,
                                                  @PathVariable String segment,
                                                  @RequestHeader(value = "Accept-Resolution",
                                                          defaultValue = DEFAULT_RESOLUTION) String resolution) {

        if (!ALLOWED_RESOLUTIONS.contains(resolution)) {
            resolution = DEFAULT_RESOLUTION;
        }

        Path path = Paths.get(HLS_DIR, videoId, resolution, segment + ".ts");
        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Resource resource = new FileSystemResource(path);

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_TYPE, "video/mp2t"
                )
                .body(resource);

    }


    // Complete Video at once : Not a good Idea
    public ResponseEntity<Resource> stream(@PathVariable Long videoId) {
        Video video = videoService.get(videoId);

        String contentType = video.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        String filePath = video.getFilePath();

        FileSystemResource resource = new FileSystemResource(filePath);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

}
