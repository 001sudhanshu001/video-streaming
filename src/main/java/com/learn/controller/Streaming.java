package com.learn.controller;

import com.learn.entity.Video;
import com.learn.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequiredArgsConstructor
public class Streaming {
//    https://www.cloudzilla.ai/dev-education/building-a-video-streaming-app-with-spring/#join-waitlist
    // https://www.codeproject.com/Articles/5341970/Streaming-Media-Files-in-Spring-Boot-Web-Applicati

    private final VideoService videoService;

    @GetMapping(value = "/play/{videoId}")
    public ResponseEntity<StreamingResponseBody> playMediaV01(
            @PathVariable
            Long videoId,
            @RequestHeader(value = "Range", required = false)
            String rangeHeader)
    {
        Video video = videoService.get(videoId);
        String contentType = video.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        String videoFilePath = video.getFilePath();
        try {
            StreamingResponseBody responseStream;

            Path filePath = Paths.get(videoFilePath);
            Long fileSize = Files.size(filePath);
            byte[] buffer = new byte[1024];
            final HttpHeaders responseHeaders = new HttpHeaders();

            if (rangeHeader == null) {
                responseHeaders.add("Content-Type", "video/mp4");
                responseHeaders.add("Content-Length", fileSize.toString());
                responseStream = os -> {
                    RandomAccessFile file = new RandomAccessFile(videoFilePath, "r");
                    try (file) {
                        long pos = 0;
                        file.seek(pos);
                        while (pos < fileSize - 1) {
                            file.read(buffer);
                            os.write(buffer);
                            pos += buffer.length;
                        }
                        os.flush();
                    } catch (Exception e) {}
                };

                return new ResponseEntity<>(responseStream, responseHeaders, HttpStatus.OK);
            }

            String[] ranges = rangeHeader.split("-");
            Long rangeStart = Long.parseLong(ranges[0].substring(6));
            Long rangeEnd;
            if (ranges.length > 1) {
                rangeEnd = Long.parseLong(ranges[1]);
            } else {
                rangeEnd = fileSize - 1;
            }

            if (fileSize < rangeEnd) {
                rangeEnd = fileSize - 1;
            }

            String contentLength = String.valueOf((rangeEnd - rangeStart) + 1);
            responseHeaders.add("Content-Type", "video/mp4");
            responseHeaders.add("Content-Length", contentLength);
            responseHeaders.add("Accept-Ranges", "bytes");
            responseHeaders.add("Content-Range", "bytes " +
                    rangeStart + "-" + rangeEnd + "/" + fileSize);

            final Long _rangeEnd = rangeEnd;
            responseStream = os -> {
                RandomAccessFile file = new RandomAccessFile(videoFilePath, "r");
                try (file) {
                    long pos = rangeStart;
                    file.seek(pos);
                    while (pos < _rangeEnd) {
                        file.read(buffer);
                        os.write(buffer);
                        pos += buffer.length;
                    }
                    os.flush();
                }
                catch (Exception e) {}
            };

            return new ResponseEntity<StreamingResponseBody>
                    (responseStream, responseHeaders, HttpStatus.PARTIAL_CONTENT);
        }
        catch (FileNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
