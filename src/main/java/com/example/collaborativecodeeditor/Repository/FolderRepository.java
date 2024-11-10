package com.example.collaborativecodeeditor.Repository;

import com.example.collaborativecodeeditor.Entity.Branch;
import com.example.collaborativecodeeditor.Entity.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<FolderEntity, Long> {
    FolderEntity findByFolderNameAndParentFolderAndBranch(String folderName, FolderEntity parentFolder, Branch branch);

    FolderEntity findByFolderNameAndParentFolderIsNullAndBranchId(String folderName, Long branchId);

    List<FolderEntity> findByBranch_IdAndParentFolder_Id(Long branchId, Long parentFolderId);

    List<FolderEntity> findByBranch_IdAndParentFolderIsNull(Long branchId);

}
