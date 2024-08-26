package com.learn.dto.response;

import com.learn.entity.Video;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseDto {
    private Video video;
    private String message;
}
