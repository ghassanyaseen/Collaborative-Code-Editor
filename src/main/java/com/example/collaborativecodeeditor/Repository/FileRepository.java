package com.example.collaborativecodeeditor.Repository;

import com.example.collaborativecodeeditor.Entity.Branch;
import com.example.collaborativecodeeditor.Entity.FileEntity;
import com.example.collaborativecodeeditor.Entity.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    FileEntity findByFileNameAndParentFolderAndBranch(String fileName, FolderEntity parentFolder, Branch branch);

    FileEntity findByFileNameAndParentFolderIsNullAndBranchId(String fileName, Long branchId);

    List<FileEntity> findByBranch_IdAndParentFolder_Id(Long branchId, Long parentFolderId);

    List<FileEntity> findByFileNameContainingIgnoreCase(String fileName);

    List<FileEntity> findByBranch_IdAndParentFolderIsNull(Long branchId);

}
