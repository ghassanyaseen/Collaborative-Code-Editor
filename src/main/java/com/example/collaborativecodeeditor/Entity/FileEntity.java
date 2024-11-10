package com.example.collaborativecodeeditor.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "files")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @Lob
    private String content;

    private String createdBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String updatedBy;

    @Enumerated(EnumType.STRING)
    private LanguageType languageType;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    @JsonIgnore
    private Branch branch;


    @ManyToOne
    @JoinColumn(name = "parent_folder_id")
    @JsonIgnore
    private FolderEntity parentFolder;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


}
