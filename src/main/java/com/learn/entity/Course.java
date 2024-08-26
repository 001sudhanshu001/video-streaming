package com.learn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "yt_courses")
@Getter @Setter
public class Course {

    @Id
    private String id;

    private String title;

}
