package com.example.collaborativecodeeditor.DTO;

import com.example.collaborativecodeeditor.Entity.FileEntity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileDTO {
    private Long id;
    private String fileName;
    private String createdBy;
    private String languageType;
    private Long parentFolderId;
    private Long branchId;


    public FileDTO(FileEntity file) {
        this.id = file.getId();
        this.fileName = file.getFileName();

        this.createdBy = file.getCreatedBy();

        this.languageType = file.getLanguageType() != null ? file.getLanguageType().toString() : "Not Specified";
        this.parentFolderId = file.getParentFolder() != null ? file.getParentFolder().getId() : null;
        this.branchId = file.getBranch().getId();

    }


    @Override
    public String toString() {
        return "FileDTO{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", languageType='" + languageType + '\'' +
                ", parentFolderId=" + parentFolderId +
                ", branchId=" + branchId +
                '}';
    }

}
