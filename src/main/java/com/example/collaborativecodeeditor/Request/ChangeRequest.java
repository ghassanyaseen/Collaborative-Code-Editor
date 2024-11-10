package com.example.collaborativecodeeditor.Request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChangeRequest {
    private String fileId;
    private int start;
    private int end;
    private String text;
    private String updatedBy;
    private String timestamp;
    private int version;
    private boolean thereIsWait;

    public ChangeRequest() {
    }

}