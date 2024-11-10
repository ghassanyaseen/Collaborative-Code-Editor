package com.example.collaborativecodeeditor.Factories;

import com.example.collaborativecodeeditor.Entity.LanguageType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CodeExecutionUtils {

    public static String getDockerImage(LanguageType language) {
        return switch (language) {
            case JAVA -> "openjdk:22";
            case JAVASCRIPT -> "node:14";
            case PYTHON -> "python";
            case C, CPP -> "gcc:latest";
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }

    public static Path writeCodeToFile(String code, LanguageType language) throws Exception {
        Path baseDir = Paths.get("/app/codes");
        Files.createDirectories(baseDir);

        String filename = switch (language) {
            case JAVA -> "Main.java";
            case JAVASCRIPT -> "script.js";
            case PYTHON -> "script.py";
            case C -> "main.c";
            case CPP -> "main.cpp";
        };

        Path filePath = baseDir.resolve(filename);
        Files.writeString(filePath, code);
        return filePath;
    }

    public static String buildCommand(LanguageType language) {
        return switch (language) {
            case JAVA -> "mkdir -p /app/com && javac -d /app/com /app/codes/Main.java && java -cp /app/com Main";
            case JAVASCRIPT -> "node /app/codes/script.js";
            case PYTHON -> "python /app/codes/script.py";
            case C -> "mkdir -p /app/com && gcc -o /app/com/main.out /app/codes/main.c && /app/com/main.out";
            case CPP -> "mkdir -p /app/com && g++ -o /app/com/main.out /app/codes/main.cpp && /app/com/main.out";
        };
    }
}
