package com.example.collaborativecodeeditor.Controllers.RESTControllers;

import com.example.collaborativecodeeditor.Entity.Comment;
import com.example.collaborativecodeeditor.Services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CommentController {

    @Autowired
    private CommentService commentService;


    @PostMapping("/api/files/{id}/comments")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR", "ROLE_VIEWER"})
    public ResponseEntity<Comment> addComment(@PathVariable Long id, @RequestParam Integer lineNumber, @RequestParam String content) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdBy = authentication.getName();
        Comment comment = commentService.addComment(id, lineNumber, content, createdBy);
        return ResponseEntity.ok(comment);
    }


    @GetMapping("/api/files/{id}/comments")
    @Secured({"ROLE_ADMIN", "ROLE_EDITOR", "ROLE_VIEWER"})
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long id) {
        List<Comment> comments = commentService.getComments(id);
        return ResponseEntity.ok(comments);
    }

}
