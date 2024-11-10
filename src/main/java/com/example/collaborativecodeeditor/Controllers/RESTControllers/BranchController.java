package com.example.collaborativecodeeditor.Controllers.RESTControllers;

import com.example.collaborativecodeeditor.DTO.BranchDTO;
import com.example.collaborativecodeeditor.Entity.Branch;
import com.example.collaborativecodeeditor.Services.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    @Autowired
    private BranchService branchService;



    @PostMapping("/create")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR"})
    public ResponseEntity<?> createBranch(@RequestParam String branchName) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdBy = authentication.getName();

        try {
            Branch branch = branchService.createBranch(branchName, createdBy);

            return ResponseEntity.ok(branch);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    @GetMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR"})
    public ResponseEntity<BranchDTO> getBranchById(@PathVariable Long id) {
        try {
            Branch branch = branchService.getBranchById(id);
            BranchDTO branchDTO = new BranchDTO(branch);
            return ResponseEntity.ok(branchDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }



    @GetMapping
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR"})
    public ResponseEntity<List<BranchDTO>> getAllBranches() {
        List<Branch> branches = branchService.getAllBranches();
        List<BranchDTO> branchDTOs = branches.stream().map(BranchDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok(branchDTOs);
    }



    @PostMapping("/merge")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR"})
    public ResponseEntity<String> mergeBranchesByName(
            @RequestParam String targetBranchName,
            @RequestParam String sourceBranchName) {
        try {

            Branch targetBranch = branchService.findByName(targetBranchName);
            if (targetBranch == null) {
                return ResponseEntity.badRequest().body("Target branch not found!");
            }

            Branch sourceBranch = branchService.findByName(sourceBranchName);
            if (sourceBranch == null) {
                return ResponseEntity.badRequest().body("Source branch not found!");
            }

            branchService.mergingBranch(targetBranch, sourceBranch);
            return ResponseEntity.ok("Branches merged successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error during merge: " + e.getMessage());
        }
    }



    @PostMapping("/clone")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR"})
    public ResponseEntity<?> cloneBranch(@RequestParam String newBranchName, @RequestParam String sourceBranchName) {
        try {
            Branch clonedBranch = branchService.cloneBranch(newBranchName, sourceBranchName);
            return ResponseEntity.ok(clonedBranch);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    @DeleteMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR"})
    public ResponseEntity<String> deleteBranchById(@PathVariable Long id) {
        try {
            branchService.deleteBranchById(id);
            return ResponseEntity.ok("Branch deleted successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error deleting branch: " + e.getMessage());
        }
    }


}

