package com.example.collaborativecodeeditor.Factories;

import com.example.collaborativecodeeditor.Entity.LanguageType;

public class LanguagesTypeFactory {

    public static String getLanguageFileExtension(LanguageType languageType) {
        return switch (languageType) {
            case JAVA -> ".java";
            case PYTHON -> ".py";
            case JAVASCRIPT -> ".js";
            case CPP -> ".cpp";
            case C -> ".c";
            default -> "";
        };
    }
}
