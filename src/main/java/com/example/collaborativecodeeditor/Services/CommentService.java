package com.example.collaborativecodeeditor.Services;

import com.example.collaborativecodeeditor.Entity.Comment;
import com.example.collaborativecodeeditor.Repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class CommentService {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Comment addComment(Long fileId, Integer lineNumber, String content, String createdBy) {
        Comment comment = new Comment();
        comment.setFileId(fileId);
        comment.setLineNumber(lineNumber);
        comment.setContent(content);
        comment.setCreatedBy(createdBy);
        comment.setTimestamp(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        messagingTemplate.convertAndSend("/topic/join/" + fileId,
                createdBy + " added a comment: \"" + content + "\" on line " + lineNumber);

        return savedComment;
    }

    public List<Comment> getComments(Long fileId) {
        return commentRepository.findByFileId(fileId);
    }
}