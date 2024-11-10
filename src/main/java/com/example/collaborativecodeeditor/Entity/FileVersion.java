package com.example.collaborativecodeeditor.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "file_versions")
public class FileVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fileId;

    @Lob
    private String content;

    private String updatedBy;

    private LocalDateTime timestamp;

    private Integer versionNumber;

    @Enumerated(EnumType.STRING)
    private LanguageType languageType;


    public FileVersion() {
        this.timestamp = LocalDateTime.now();
    }
}
