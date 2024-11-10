package com.example.collaborativecodeeditor.DTO;

import com.example.collaborativecodeeditor.Entity.FolderEntity;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class FolderDTO {
    private Long id;
    private String folderName;
    private Long parentFolderId;
    private Long branchId;

    public FolderDTO(FolderEntity folder) {
        this.id = folder.getId();
        this.folderName = folder.getFolderName();
        this.parentFolderId = folder.getParentFolder() != null ? folder.getParentFolder().getId() : null;
        this.branchId = folder.getBranch().getId();
    }

}
