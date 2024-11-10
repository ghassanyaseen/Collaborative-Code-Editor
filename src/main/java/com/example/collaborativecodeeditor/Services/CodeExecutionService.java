package com.example.collaborativecodeeditor.Services;

import com.example.collaborativecodeeditor.Entity.LanguageType;
import com.example.collaborativecodeeditor.Factories.CodeExecutionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class CodeExecutionService {

    @Autowired
    private DockerContainerManager dockerContainerManager;

    private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public String runCodeInDocker(String code, LanguageType language, String branchId) {
        String lockKey = language.name() + "_" + branchId;
        ReentrantLock lock = lockMap.computeIfAbsent(lockKey, k -> new ReentrantLock());

        lock.lock();
        try {
            String dockerImage = CodeExecutionUtils.getDockerImage(language);
            Path filename = CodeExecutionUtils.writeCodeToFile(code, language);
            String command = CodeExecutionUtils.buildCommand(language);

            dockerContainerManager.startContainer(language, dockerImage, branchId);
            String containerName = dockerContainerManager.getContainerName(language, branchId);

            if (!dockerContainerManager.isContainerRunning(containerName)) {
                return "Container is being created. Please try again shortly.";
            }

            return executeCommandInContainer(containerName, command);
        } catch (Exception e) {
            return "Error running the code: " + e.getMessage();
        } finally {
            lock.unlock();
        }
    }

    private String executeCommandInContainer(String containerName, String command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("docker", "exec", containerName, "bash", "-c", command);
        Process process = processBuilder.start();

        StringBuilder output = readProcessOutput(process.getInputStream());
        StringBuilder errorOutput = readProcessOutput(process.getErrorStream());

        process.waitFor();

        if (!errorOutput.isEmpty()) {
            return "Error running the code: " + errorOutput.toString();
        }

        return output.toString();
    }

    private StringBuilder readProcessOutput(InputStream inputStream) throws Exception {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output;
    }
}
