package com.example.collaborativecodeeditor.Controllers.CodeControllers;

import com.example.collaborativecodeeditor.Entity.Dashboard;
import com.example.collaborativecodeeditor.Entity.FileEntity;
import com.example.collaborativecodeeditor.Request.ChangeRequest;
import com.example.collaborativecodeeditor.Services.CodeEditorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CodeEditorController {

    @Autowired
    private CodeEditorService codeEditorService;

    @MessageMapping("/code-edit")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR"})
    public void updateCode(@Payload ChangeRequest changeRequest) {
        codeEditorService.updateCode(changeRequest);
    }

    @GetMapping("/api/files/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR", "ROLE_VIEWER"})
    public ResponseEntity<FileEntity> getFileById(@PathVariable Long id) {
        return ResponseEntity.ok(codeEditorService.getFileById(id));
    }

    @GetMapping("/api/files/{fileId}/dashboard")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR"})
    public ResponseEntity<Dashboard> getDashboardByFileId(@PathVariable Long fileId) {
        Dashboard dashboard = codeEditorService.getDashboardByFileId(fileId);
        return dashboard != null ? ResponseEntity.ok(dashboard) : ResponseEntity.notFound().build();
    }

    @PostMapping("/api/files/{id}/revert/{versionId}")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR"})
    public ResponseEntity<FileEntity> revertFile(@PathVariable Long id, @PathVariable Long versionId) {
        try {
            return ResponseEntity.ok(codeEditorService.revertFile(id, versionId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @MessageMapping("/refresh")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR", "ROLE_VIEWER"})
    public void notifyRefresh(@PathVariable Long id, @Payload String message) {
        codeEditorService.notifyRefresh(id, message);
    }
}
