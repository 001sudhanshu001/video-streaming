package com.learn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "yt_videos")
@Getter @Setter
public class Video {

    @Id
    private String videoId;
    private String title;
    private String description;
    private String contentType;
    private String filePath;

}
