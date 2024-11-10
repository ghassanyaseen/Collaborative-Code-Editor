package com.example.collaborativecodeeditor.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fileId;
    private Integer lineNumber;
    private String content;
    private String createdBy;
    private LocalDateTime timestamp;
}

