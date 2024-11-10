package com.example.collaborativecodeeditor.Controllers.RESTControllers;

import com.example.collaborativecodeeditor.DTO.FolderDTO;
import com.example.collaborativecodeeditor.Entity.FolderEntity;
import com.example.collaborativecodeeditor.Services.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    @Autowired
    private FolderService folderService;

    @PostMapping("/create")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR"})
    public ResponseEntity<String> createFolder(@RequestParam String folderName,
                                               @RequestParam(required = false) Long parentFolderId,
                                               @RequestParam Long branchId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdBy = authentication.getName();

        try {
            FolderEntity folder = folderService.createFolder(folderName, parentFolderId, branchId, createdBy);

            if (folder != null) {
                return ResponseEntity.ok("Folder created successfully: " + folder.getFolderName());
            } else {
                return ResponseEntity.badRequest().body("Error: The folder already exists under the selected parent.");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }



    @GetMapping("/branch/{branchId}/parent/{parentFolderId}")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR", "ROLE_VIEWER"})
    public ResponseEntity<List<FolderDTO>> getFoldersInBranch(@PathVariable Long branchId, @PathVariable Long parentFolderId) {
        List<FolderEntity> folders = folderService.getFoldersInBranch(branchId, parentFolderId);
        List<FolderDTO> folderDTOs = folders.stream()
                .map(FolderDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(folderDTOs);
    }


    @GetMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR", "ROLE_VIEWER"})
    public ResponseEntity<FolderEntity> getFolderById(@PathVariable Long id) {
        FolderEntity folder = folderService.getFolderById(id);
        return ResponseEntity.ok(folder);
    }


    @DeleteMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR"})
    public ResponseEntity<String> deleteFolderById(@PathVariable Long id) {
        try {
            folderService.deleteFolderById(id);
            return ResponseEntity.ok("Folder deleted successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error deleting folder: " + e.getMessage());
        }
    }
}
