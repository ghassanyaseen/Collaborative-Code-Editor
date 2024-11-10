package com.example.collaborativecodeeditor.Services;

import com.example.collaborativecodeeditor.Entity.Branch;
import com.example.collaborativecodeeditor.Entity.FolderEntity;
import com.example.collaborativecodeeditor.Repository.BranchRepository;
import com.example.collaborativecodeeditor.Repository.FileRepository;
import com.example.collaborativecodeeditor.Repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class FolderService {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private FileRepository fileRepository;

    private final ReentrantLock folderLock = new ReentrantLock();

    @Transactional
    public FolderEntity createFolder(String folderName, Long parentFolderId, Long branchId, String createdBy) {
        folderLock.lock();
        try {
            if (folderExists(folderName, parentFolderId, branchId)) {
                throw new RuntimeException("A folder with this name already exists under the selected parent in this branch.");
            }

            FolderEntity folder = new FolderEntity();
            folder.setFolderName(folderName);
            folder.setCreatedBy(createdBy);
            folder.setCreatedAt(LocalDateTime.now());
            folder.setUpdatedAt(LocalDateTime.now());

            Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new RuntimeException("Branch not found"));
            folder.setBranch(branch);

            if (parentFolderId != null && parentFolderId != 0) {
                FolderEntity parentFolder = folderRepository.findById(parentFolderId)
                        .orElseThrow(() -> new RuntimeException("Parent folder not found"));
                folder.setParentFolder(parentFolder);
                folder.setBranch(parentFolder.getBranch());
            }

            return folderRepository.save(folder);
        } finally {
            folderLock.unlock();
        }
    }


    @Transactional
    public void deleteFolderById(Long folderId) {
        FolderEntity folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));
        deleteFolder(folder);
    }



    public void deleteFolder(FolderEntity folder) {
        for (FolderEntity subFolder : folder.getSubFolders()) {
            deleteFolder(subFolder);
        }

        fileRepository.deleteAll(folder.getFiles());
        folderRepository.delete(folder);
    }

    public List<FolderEntity> getFoldersInBranch(Long branchId, Long parentFolderId) {
        if (parentFolderId == null || parentFolderId == 0) {
            return folderRepository.findByBranch_IdAndParentFolderIsNull(branchId);
        } else {
            return folderRepository.findByBranch_IdAndParentFolder_Id(branchId, parentFolderId);
        }
    }

    private boolean folderExists(String folderName, Long parentFolderId, Long branchId) {
        if (parentFolderId == null || parentFolderId == 0) {
            return folderRepository.findByFolderNameAndParentFolderIsNullAndBranchId(folderName, branchId) != null;
        } else {
            FolderEntity parentFolder = folderRepository.findById(parentFolderId)
                    .orElseThrow(() -> new RuntimeException("Parent folder not found"));
            return folderRepository.findByFolderNameAndParentFolderAndBranch(folderName, parentFolder, branchRepository.findById(branchId).orElseThrow()) != null;
        }
    }

    public List<FolderEntity> getFolders(Long parentFolderId) {
        if (parentFolderId == null) {
            return folderRepository.findAll();
        }
        FolderEntity parentFolder = folderRepository.findById(parentFolderId)
                .orElseThrow(() -> new RuntimeException("Parent folder not found"));
        return parentFolder.getSubFolders();
    }

    public FolderEntity getFolderById(Long id) {
        return folderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Folder not found"));
    }
}
