package com.example.collaborativecodeeditor.Repository;

import com.example.collaborativecodeeditor.Entity.FileVersion;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileVersionRepository extends JpaRepository<FileVersion, Long> {
    List<FileVersion> findByFileId(Long fileId);

    @Modifying
    @Transactional
    void deleteByFileId(Long fileId);

}
