package com.example.collaborativecodeeditor.Request;

import com.example.collaborativecodeeditor.Entity.LanguageType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeRequest {
    private String fileId;
    private String code;
    private String branchId;
    private LanguageType language;
}
