package com.example.collaborativecodeeditor.Services;

import com.example.collaborativecodeeditor.Entity.FileEntity;
import com.example.collaborativecodeeditor.Entity.FileVersion;
import com.example.collaborativecodeeditor.Repository.FileVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class FileVersionService {

    @Autowired
    private FileVersionRepository fileVersionRepository;

    @Autowired
    @Lazy
    private FileService fileService;


    private final ReentrantLock versionLock = new ReentrantLock();



    public List<FileVersion> getFileVersionsByFileId(Long fileId) {
        return fileVersionRepository.findByFileId(fileId);
    }

    @Transactional
    public FileEntity revertToVersion(Long fileId, Long versionId) {
        versionLock.lock();
        try {
            FileVersion version = fileVersionRepository.findById(versionId)
                    .orElseThrow(() -> new RuntimeException("Version not found"));

            FileEntity file = fileService.getFileById(fileId);
            file.setContent(version.getContent());
            file.setUpdatedAt(LocalDateTime.now());
            file.setUpdatedBy(version.getUpdatedBy());

            return fileService.saveFile(file);
        } finally {
            versionLock.unlock();
        }
    }

    public Integer getNextVersionNumber(Long fileId) {
        if (fileId == null) {
            throw new IllegalArgumentException("fileId cannot be null");
        }
        List<FileVersion> versions = fileVersionRepository.findByFileId(fileId);

        Integer maxVersionNumber = versions.stream()
                .map(FileVersion::getVersionNumber)
                .max(Integer::compareTo)
                .orElse(0);

        return maxVersionNumber + 1;
    }

    public FileVersion getVersionById(Long versionId) {
        return fileVersionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Version not found"));
    }

    public void deleteVersion(Long versionId) {
        versionLock.lock();
        try {
            fileVersionRepository.deleteById(versionId);
        } finally {
            versionLock.unlock();
        }
    }

    @Transactional
    public void saveVersion(FileVersion version) {
        versionLock.lock();
        try {
            fileVersionRepository.save(version);
        } finally {
            versionLock.unlock();
        }
    }
}
