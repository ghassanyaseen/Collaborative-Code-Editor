package com.example.collaborativecodeeditor.Services;

import com.example.collaborativecodeeditor.Entity.*;
import com.example.collaborativecodeeditor.Factories.LanguagesTypeFactory;
import com.example.collaborativecodeeditor.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.InputStreamResource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class FileService {
    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    @Lazy
    private FileVersionService fileVersionService;

    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private FileVersionRepository fileVersionRepository;


    private final ConcurrentHashMap<Long, ReentrantLock> fileLocks = new ConcurrentHashMap<>();


    private final ReentrantLock creationLock = new ReentrantLock();

    @Transactional
    public FileEntity createFile(String fileName, String content, Long parentFolderId, Long branchId, String createdBy, LanguageType languageType) {
        creationLock.lock();
        try {

            if (!isValidFileName(fileName)) {
                throw new RuntimeException("Invalid file name. The file name contains invalid characters or exceeds length limits.");
            }

            String fullFileName = fileName + LanguagesTypeFactory.getLanguageFileExtension(languageType);


            if (fileExists(fullFileName, parentFolderId, branchId)) {
                throw new RuntimeException("A file with this name already exists in the selected parent folder within this branch.");
            }


            FileEntity fileEntity = new FileEntity();
            fileEntity.setFileName(fullFileName);
            fileEntity.setContent(content);
            fileEntity.setCreatedAt(LocalDateTime.now());
            fileEntity.setUpdatedAt(LocalDateTime.now());
            fileEntity.setCreatedBy(createdBy);
            fileEntity.setLanguageType(languageType);

            Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new RuntimeException("Branch not found"));
            fileEntity.setBranch(branch);

            if (parentFolderId != null && parentFolderId != 0) {
                FolderEntity parentFolder = folderRepository.findById(parentFolderId)
                        .orElseThrow(() -> new RuntimeException("Parent folder not found"));

                fileEntity.setParentFolder(parentFolder);
                fileEntity.setBranch(parentFolder.getBranch());
            }

            return fileRepository.save(fileEntity);
        } finally {
            creationLock.unlock();
        }
    }

    @Transactional
    public FileEntity updateFile(Long id, String fileName, String content, String updatedBy) {

        ReentrantLock lock = fileLocks.computeIfAbsent(id, k -> new ReentrantLock());
        lock.lock();
        try {

            FileEntity file = fileRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            boolean hasChanges = !fileName.equals(file.getFileName()) || !content.equals(file.getContent());

            if (hasChanges) {
                file.setFileName(fileName);
                file.setContent(content);
                file.setUpdatedBy(updatedBy);
                file.setUpdatedAt(LocalDateTime.now());

                FileVersion version = new FileVersion();
                version.setFileId(file.getId());
                version.setContent(content);
                version.setUpdatedBy(updatedBy);
                version.setTimestamp(LocalDateTime.now());
                version.setVersionNumber(fileVersionService.getNextVersionNumber(file.getId()));
                fileVersionService.saveVersion(version);

                return fileRepository.save(file);
            }
            return file;
        } finally {
            lock.unlock();
            fileLocks.remove(id); // Clean up the lock if no longer needed
        }
    }

    public ResponseEntity<Resource> downloadFile(String filename) {
        try {
            FileEntity file = findFileByName(filename);

            if (file == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            byte[] content = file.getContent().getBytes();
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(content));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    public FileEntity findFileByName(String fileName) {
        return fileRepository.findByFileNameContainingIgnoreCase(fileName).stream().findFirst().orElse(null);
    }

    @Transactional
    public void deleteFile(Long id) {
        try {
            fileVersionRepository.deleteByFileId(id);
            dashboardRepository.deleteById(id);
            fileRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("File not found.");
        }
    }

    public List<FileEntity> getFilesInBranch(Long branchId, Long parentFolderId) {
        if (parentFolderId == null || parentFolderId == 0) {
            return fileRepository.findByBranch_IdAndParentFolderIsNull(branchId);
        } else {
            return fileRepository.findByBranch_IdAndParentFolder_Id(branchId, parentFolderId);
        }
    }

    public FileEntity getFileById(Long id) {
        return fileRepository.findById(id).orElseThrow(() -> new RuntimeException("File not found"));
    }

    public List<FileEntity> getAllFiles() {
        return fileRepository.findAll();
    }

    public boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }

        String invalidCharsPattern = "^[a-zA-Z0-9_-]+$";

        if (!fileName.matches(invalidCharsPattern)) {
            return false;
        }

        if (fileName.length() > 255) {
            return false;
        }

        if (fileName.startsWith(" ") || fileName.endsWith(" ")) {
            return false;
        }

        return true;
    }

    private boolean fileExists(String fullFileName, Long parentFolderId, Long branchId) {
        if (parentFolderId == null || parentFolderId == 0) {
            return fileRepository.findByFileNameAndParentFolderIsNullAndBranchId(fullFileName, branchId) != null;
        } else {
            FolderEntity parentFolder = folderRepository.findById(parentFolderId)
                    .orElseThrow(() -> new RuntimeException("Parent folder not found"));

            return fileRepository.findByFileNameAndParentFolderAndBranch(fullFileName, parentFolder, parentFolder.getBranch()) != null;
        }
    }

    public FileEntity saveFile(FileEntity file) {
        return fileRepository.save(file);
    }
}
