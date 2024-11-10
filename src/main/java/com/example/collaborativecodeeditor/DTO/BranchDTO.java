package com.example.collaborativecodeeditor.DTO;

import com.example.collaborativecodeeditor.Entity.Branch;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class BranchDTO {
    private Long id;
    private String branchName;
    private String createdBy;
    private List<FolderDTO> subFolders;
    private List<FileDTO> subFiles;


    public BranchDTO(Branch branch) {
        this.id = branch.getId();
        this.branchName = branch.getBranchName();
        this.createdBy = branch.getCreatedBy();
        this.subFolders = branch.getSubFolders().stream().map(FolderDTO::new).collect(Collectors.toList());
        this.subFiles = branch.getSubFiles().stream()
                .map(FileDTO::new)
                .collect(Collectors.toList());
    }


    @Override
    public String toString() {
        return "BranchDTO{" +
                "id=" + id +
                ", branchName='" + branchName + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", subFolders=" + subFolders.stream().map(FolderDTO::getFolderName).toList() +
                ", subFiles=" + subFiles.stream().map(FileDTO::getFileName).toList() +
                '}';
    }
}
