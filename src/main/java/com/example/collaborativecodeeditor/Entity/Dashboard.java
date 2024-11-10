package com.example.collaborativecodeeditor.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "dashboard")
public class Dashboard {

    @Id
    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Lob
    @Column(name = "content")
    private String content;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;


    @ManyToOne
    @JoinColumn(name = "file_id", referencedColumnName = "id", insertable = false, updatable = false)
    private FileEntity file;

    public Dashboard() {
        this.timestamp = LocalDateTime.now();
    }


}
