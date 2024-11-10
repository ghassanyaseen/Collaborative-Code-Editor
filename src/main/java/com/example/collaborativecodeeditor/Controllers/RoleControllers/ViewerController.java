package com.example.collaborativecodeeditor.Controllers.RoleControllers;

import com.example.collaborativecodeeditor.Entity.Branch;
import com.example.collaborativecodeeditor.Entity.FileEntity;
import com.example.collaborativecodeeditor.Entity.FolderEntity;
import com.example.collaborativecodeeditor.Services.BranchService;
import com.example.collaborativecodeeditor.Services.FileService;
import com.example.collaborativecodeeditor.Services.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("viewer")
public class ViewerController {


    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private BranchService branchService;

    @Autowired
    FolderService folderService;

    @Autowired
    FileService fileService;


    @GetMapping("/branch-viewer")
    @Secured({"ROLE_ADMIN", "ROLE_VIEWER"})
    public String branchPage() {
        return "Viewer/branch-viewer";
    }

    @GetMapping("/branch-detail-viewer/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_VIEWER"})
    public String branchDetailsPage(@PathVariable Long id, Model model) {

        Branch branch = branchService.getBranchById(id);


        model.addAttribute("branch", branch);

        return "Viewer/branch-detail-viewer";
    }

    @GetMapping("/folder-viewer/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_VIEWER"})
    public String viewFolder(@PathVariable Long id, Model model) {
        try {
            FolderEntity folder = folderService.getFolderById(id);

            List<FolderEntity> subFolders;
            if (folder.getParentFolder() != null) {
                subFolders = folderService.getFolders(folder.getParentFolder().getId());
            } else {
                subFolders = folderService.getFolders(id);
            }

            List<FileEntity> files = folder.getFiles();

            model.addAttribute("folderName", folder.getFolderName());
            model.addAttribute("createdBy", folder.getCreatedBy());
            model.addAttribute("subFolders", subFolders);
            model.addAttribute("files", files);

            return "Viewer/folder-viewer";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            return "error";
        }
    }



    @GetMapping("/file-viewer/{fileId}")
    @Secured({"ROLE_ADMIN", "ROLE_VIEWER"})
    public String viewFile(@PathVariable Long fileId, Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            FileEntity file = fileService.getFileById(fileId);
            if (file == null) {
                throw new IllegalArgumentException("File not found");
            }

            model.addAttribute("file", file);
            model.addAttribute("username", username);


            Branch branch = file.getBranch();
            if (branch != null) {
                model.addAttribute("branchId", branch.getId());
            }

            messagingTemplate.convertAndSend("/topic/join/" + fileId, username + " has joined the session for file ID: " + fileId);

            return "Viewer/file-viewer";

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }
}
