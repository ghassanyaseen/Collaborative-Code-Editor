package com.example.collaborativecodeeditor.Controllers.RESTControllers;

import com.example.collaborativecodeeditor.Entity.FileVersion;
import com.example.collaborativecodeeditor.Services.FileVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FileVersionsController {


    @Autowired
    private FileVersionService fileVersionService;


    @GetMapping("/api/files/{fileId}/versions")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR", "ROLE_VIEWER"})
    public ResponseEntity<List<FileVersion>> getFileVersions(@PathVariable Long fileId) {
        List<FileVersion> versions = fileVersionService.getFileVersionsByFileId(fileId);
        return ResponseEntity.ok(versions);
    }



    @DeleteMapping("/api/files/{fileId}/versions/{versionId}")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR"})
    public ResponseEntity<Void> deleteVersion(@PathVariable Long fileId, @PathVariable Long versionId) {
        System.out.println("Attempting to delete version ID: " + versionId + " for file ID: " + fileId);
        try {
            System.out.println("Attempting to delete version ID: " + versionId);
            fileVersionService.deleteVersion(versionId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("Error deleting version: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @GetMapping("/api/files/version/{versionId}")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR", "ROLE_VIEWER"})
    public ResponseEntity<FileVersion> getVersionById(@PathVariable Long versionId) {
        FileVersion version = fileVersionService.getVersionById(versionId);
        return ResponseEntity.ok(version);
    }

}
