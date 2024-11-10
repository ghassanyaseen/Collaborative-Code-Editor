package com.example.collaborativecodeeditor.Controllers.CodeControllers;

import com.example.collaborativecodeeditor.Entity.LanguageType;
import com.example.collaborativecodeeditor.Request.CodeRequest;
import com.example.collaborativecodeeditor.Services.CodeExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/execute")
public class CodeExecutionController {

    @Autowired
    private CodeExecutionService codeExecutionService;

    @PostMapping
    public ResponseEntity<String> executeCode(@RequestBody CodeRequest request) {
        LanguageType language = request.getLanguage();
        String code = request.getCode();
        String branch = request.getBranchId();


        try {
            String output = codeExecutionService.runCodeInDocker(code, language, branch);
            return ResponseEntity.ok(output);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during code execution: " + e.getMessage());
        }
    }
}
