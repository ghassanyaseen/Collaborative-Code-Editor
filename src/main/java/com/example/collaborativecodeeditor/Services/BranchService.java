package com.example.collaborativecodeeditor.Services;

import com.example.collaborativecodeeditor.Entity.Branch;
import com.example.collaborativecodeeditor.Entity.FileEntity;
import com.example.collaborativecodeeditor.Entity.FolderEntity;
import com.example.collaborativecodeeditor.Repository.BranchRepository;
import com.example.collaborativecodeeditor.Repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class BranchService {

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private FolderService folderService;

    @Autowired
    private FileRepository fileRepository;

    private final ReentrantLock branchLock = new ReentrantLock();


    @Transactional
    public Branch createBranch(String branchName, String createdBy) {
        branchLock.lock();
        try {
            if (branchRepository.findByBranchName(branchName) != null) {
                throw new RuntimeException("A branch with this name already exists.");
            }

            Branch branch = new Branch();
            branch.setBranchName(branchName);
            branch.setCreatedBy(createdBy);
            return branchRepository.save(branch);
        } finally {
            branchLock.unlock();
        }
    }


    public Branch getBranchById(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
    }


    public Branch findByName(String branchName) {
        return branchRepository.findByBranchName(branchName);
    }


    @Transactional
    public void deleteBranchById(Long id) {
        branchLock.lock();
        try {
            Branch branch = branchRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Branch not found"));
            deleteBranch(branch);
        } finally {
            branchLock.unlock();
        }
    }


    public List<FileEntity> getRootFilesForBranch(Long branchId) {
        return fileRepository.findByBranch_IdAndParentFolderIsNull(branchId);
    }



    public void deleteBranch(Branch branch) {
        branch.getSubFiles().clear();
        for (FolderEntity rootFolder : branch.getSubFolders()) {
            if (rootFolder.getParentFolder() == null) {
                folderService.deleteFolder(rootFolder);
            }
        }
        branchRepository.delete(branch);
    }



    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }


    @Transactional
    public void mergingBranch(Branch targetBranch, Branch sourceBranch) {
        branchLock.lock();
        try {

            List<FileEntity> rootFiles = getRootFilesForBranch(sourceBranch.getId());
            for (FileEntity sourceFile : rootFiles) {
                mergeFile(sourceFile, null, targetBranch);
            }

            // Merge root folders
            for (FolderEntity sourceRootFolder : sourceBranch.getSubFolders()) {
                if (sourceRootFolder.getParentFolder() == null) {
                    FolderEntity matchingTargetRootFolder = findMatchingFolder(sourceRootFolder.getFolderName(), targetBranch.getSubFolders());

                    if (matchingTargetRootFolder == null) {
                        FolderEntity newRootFolder = getFolderEntity(targetBranch, sourceRootFolder);
                        targetBranch.getSubFolders().add(newRootFolder);

                        mergeFolders(sourceRootFolder, newRootFolder, targetBranch);
                    } else {
                        mergeFolders(sourceRootFolder, matchingTargetRootFolder, targetBranch);
                    }
                }
            }


            branchRepository.save(targetBranch);
        } catch (Exception e) {
            throw new RuntimeException("Failed to merge branches", e);
        } finally {
            branchLock.unlock();
        }
        CompletableFuture.completedFuture(null);
    }

    private static FolderEntity getFolderEntity(Branch targetBranch, FolderEntity sourceRootFolder) {
        FolderEntity newRootFolder = new FolderEntity();
        newRootFolder.setFolderName(sourceRootFolder.getFolderName());
        newRootFolder.setCreatedBy(sourceRootFolder.getCreatedBy());
        newRootFolder.setCreatedAt(sourceRootFolder.getCreatedAt());
        newRootFolder.setUpdatedAt(sourceRootFolder.getUpdatedAt());
        newRootFolder.setBranch(targetBranch);
        newRootFolder.setParentFolder(null);
        return newRootFolder;
    }

    private void mergeFolders(FolderEntity sourceFolder, FolderEntity targetFolder, Branch targetBranch) {
        // Merge Files inside the folder
        for (FileEntity sourceFile : sourceFolder.getFiles()) {
            mergeFile(sourceFile, targetFolder, targetBranch);
        }

        // Merge Subfolders
        for (FolderEntity sourceSubFolder : sourceFolder.getSubFolders()) {
            FolderEntity matchingTargetSubFolder = findMatchingFolder(sourceSubFolder.getFolderName(), targetFolder.getSubFolders());

            if (matchingTargetSubFolder == null) {
                FolderEntity newSubFolder = new FolderEntity();
                newSubFolder.setFolderName(sourceSubFolder.getFolderName());
                newSubFolder.setCreatedBy(sourceSubFolder.getCreatedBy());
                newSubFolder.setCreatedAt(LocalDateTime.now());
                newSubFolder.setUpdatedAt(LocalDateTime.now());
                newSubFolder.setParentFolder(targetFolder);
                newSubFolder.setBranch(targetBranch);
                targetFolder.getSubFolders().add(newSubFolder);

                mergeFolders(sourceSubFolder, newSubFolder, targetBranch);
            } else {
                mergeFolders(sourceSubFolder, matchingTargetSubFolder, targetBranch);
            }
        }

    }

    private void mergeFile(FileEntity sourceFile, FolderEntity parentFolder, Branch targetBranch) {
        boolean fileExists = false;

        List<FileEntity> targetFiles = parentFolder != null ? parentFolder.getFiles() : targetBranch.getSubFiles();

        for (FileEntity targetFile : targetFiles) {
            if (targetFile.getFileName().equals(sourceFile.getFileName())) {
                fileExists = true;

                if (isTheFileUpdated(sourceFile, targetFile)) {
                    targetFile.setContent(sourceFile.getContent());
                    targetFile.setUpdatedBy(sourceFile.getUpdatedBy());
                    targetFile.setUpdatedAt(sourceFile.getUpdatedAt());
                    targetFile.setLanguageType(sourceFile.getLanguageType());
                }
                break;
            }
        }

        // If the file does not exist, create a new one
        if (!fileExists) {
            FileEntity newFile = new FileEntity();
            newFile.setFileName(sourceFile.getFileName());
            newFile.setContent(sourceFile.getContent());
            newFile.setCreatedBy(sourceFile.getCreatedBy());
            newFile.setCreatedAt(LocalDateTime.now());
            newFile.setUpdatedBy(sourceFile.getUpdatedBy());
            newFile.setUpdatedAt(LocalDateTime.now());
            newFile.setLanguageType(sourceFile.getLanguageType());
            newFile.setBranch(targetBranch);

            if (parentFolder != null) {
                newFile.setParentFolder(parentFolder);
                parentFolder.getFiles().add(newFile);
            } else {
                targetBranch.getSubFiles().add(newFile);
            }
        }
    }

    private FolderEntity findMatchingFolder(String folderName, List<FolderEntity> targetFolders) {
        for (FolderEntity targetFolder : targetFolders) {
            if (targetFolder.getFolderName().equals(folderName)) {
                return targetFolder;
            }
        }
        return null;
    }

    private boolean isTheFileUpdated(FileEntity sourceFile, FileEntity targetFile) {
        return sourceFile.getUpdatedAt().isAfter(targetFile.getUpdatedAt());
    }


    @Transactional
    public Branch cloneBranch(String newBranchName, String sourceBranchName) {
        branchLock.lock();
        try {
            // Check if the source branch exists
            Branch sourceBranch = branchRepository.findByBranchName(sourceBranchName);
            if (sourceBranch == null) {
                throw new RuntimeException("Source branch not found.");
            }

            // Check if the new branch name already exists
            if (branchRepository.findByBranchName(newBranchName) != null) {
                throw new RuntimeException("A branch with this name already exists.");
            }

            // Proceed with cloning the branch
            Branch newBranch = new Branch();
            newBranch.setBranchName(newBranchName);
            newBranch.setCreatedBy(sourceBranch.getCreatedBy());
            newBranch.setCreatedAt(LocalDateTime.now());
            newBranch.setUpdatedAt(LocalDateTime.now());

            newBranch = branchRepository.save(newBranch);

            cloneFoldersAndFiles(sourceBranch, newBranch);

            return newBranch;
        } finally {
            branchLock.unlock();
        }
    }


    private void cloneFoldersAndFiles(Branch sourceBranch, Branch targetBranch) {
        // Clone root folders
        for (FolderEntity sourceFolder : sourceBranch.getSubFolders()) {
            if (sourceFolder.getParentFolder() == null) {
                FolderEntity clonedFolder = cloneFolder(sourceFolder, targetBranch, null);
                targetBranch.getSubFolders().add(clonedFolder);
            }
        }

        // Clone root files
        List<FileEntity> rootFiles = getRootFilesForBranch(sourceBranch.getId());
        for (FileEntity sourceFile : rootFiles) {
            cloneFile(sourceFile, targetBranch, null); // Clone root files with null parentFolder
        }

        // Save the target branch with cloned folders and files
        branchRepository.save(targetBranch);
    }

    private FolderEntity cloneFolder(FolderEntity sourceFolder, Branch targetBranch, FolderEntity parentFolder) {
        FolderEntity clonedFolder = new FolderEntity();
        clonedFolder.setFolderName(sourceFolder.getFolderName());
        clonedFolder.setCreatedBy(sourceFolder.getCreatedBy());
        clonedFolder.setCreatedAt(LocalDateTime.now());
        clonedFolder.setUpdatedAt(LocalDateTime.now());
        clonedFolder.setBranch(targetBranch);
        clonedFolder.setParentFolder(parentFolder);

        // Clone subfolders
        for (FolderEntity subFolder : sourceFolder.getSubFolders()) {
            FolderEntity clonedSubFolder = cloneFolder(subFolder, targetBranch, clonedFolder);
            clonedFolder.getSubFolders().add(clonedSubFolder);
        }

        // Clone files
        for (FileEntity sourceFile : sourceFolder.getFiles()) {
            cloneFile(sourceFile, targetBranch, clonedFolder);
        }

        return clonedFolder;
    }

    private void cloneFile(FileEntity sourceFile, Branch targetBranch, FolderEntity parentFolder) {
        FileEntity clonedFile = new FileEntity();
        clonedFile.setFileName(sourceFile.getFileName());
        clonedFile.setContent(sourceFile.getContent());
        clonedFile.setCreatedBy(sourceFile.getCreatedBy());
        clonedFile.setCreatedAt(LocalDateTime.now());
        clonedFile.setLanguageType(sourceFile.getLanguageType());
        clonedFile.setBranch(targetBranch);

        if (parentFolder != null) {
            clonedFile.setParentFolder(parentFolder);
            parentFolder.getFiles().add(clonedFile);
        } else {
            targetBranch.getSubFiles().add(clonedFile);
        }
    }

}
