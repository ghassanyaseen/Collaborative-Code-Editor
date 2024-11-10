package com.example.collaborativecodeeditor.Controllers.RESTControllers;

import com.example.collaborativecodeeditor.DTO.FileDTO;
import com.example.collaborativecodeeditor.Entity.FileEntity;
import com.example.collaborativecodeeditor.Entity.LanguageType;
import com.example.collaborativecodeeditor.Services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;


    @PostMapping("/create")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR"})
    public ResponseEntity<String> createFile(
            @RequestParam String fileName,
            @RequestParam String content,
            @RequestParam(required = false) Long parentFolderId,
            @RequestParam Long branchId,
            @RequestParam LanguageType languageType) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdBy = authentication.getName();

        try {
            FileEntity file = fileService.createFile(fileName, content, parentFolderId, branchId, createdBy, languageType);

            return ResponseEntity.ok("File created successfully: " + file.getFileName());

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    @PostMapping("/{id}/edit")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR"})
    public ResponseEntity<?> editFile(
            @PathVariable Long id,
            @RequestParam String fileName,
            @RequestParam String content) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String updatedBy = authentication.getName();

            FileEntity updatedFile = fileService.updateFile(id, fileName, content, updatedBy);
            return ResponseEntity.ok(new FileDTO(updatedFile));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update file.");
        }
    }



    @DeleteMapping("/delete/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR"})
    public ResponseEntity<String> deleteFile(@PathVariable Long id) {
        try {
            fileService.deleteFile(id);
            return ResponseEntity.ok("File deleted successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error deleting file: " + e.getMessage());
        }
    }


    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String filename) {
        return fileService.downloadFile(filename);
    }





    @GetMapping("/branch/{branchId}/parent/{parentFolderId}")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR", "ROLE_VIEWER"})
    public ResponseEntity<List<FileDTO>> getFilesInBranch(@PathVariable Long branchId, @PathVariable Long parentFolderId) {
        List<FileEntity> files = fileService.getFilesInBranch(branchId, parentFolderId);
        List<FileDTO> fileDTOs = files.stream()
                .map(FileDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(fileDTOs);
    }



    @GetMapping("/api/files/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR", "ROLE_VIEWER"})
    public ResponseEntity<FileDTO> getFileDetails(@PathVariable Long id) {
        FileEntity fileEntity = fileService.getFileById(id);

        FileDTO fileDTO = new FileDTO(fileEntity);
        return ResponseEntity.ok(fileDTO);
    }



    @GetMapping("/all")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR", "ROLE_VIEWER"})
    public ResponseEntity<List<FileEntity>> getAllFiles() {
        List<FileEntity> files = fileService.getAllFiles();
        return ResponseEntity.ok(files);
    }

}