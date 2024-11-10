package com.example.collaborativecodeeditor.Services;

import com.example.collaborativecodeeditor.Entity.LanguageType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class DockerContainerManager {
    private final Map<String, Process> runningContainers = new HashMap<>();
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final int TIMEOUT_MINUTES = 1;

    public void startContainer(LanguageType language, String imageName, String branchId) throws Exception {
        String containerName = getContainerName(language, branchId);

        // Start the container if not already running
        if (!runningContainers.containsKey(containerName)) {
            startNewContainer(containerName, imageName);
        } else {
            resetTimer(containerName);
        }
    }

    private void startNewContainer(String containerName, String imageName) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker",
                "run",
                "--name", containerName,
                "--volumes-from", "collaborative-code-editor-app-1:ro",
                imageName,
                "tail",
                "-f",
                "/dev/null"
        );
        Process process = processBuilder.start();
        runningContainers.put(containerName, process);


        scheduleContainerTermination(containerName);
    }

    String getContainerName(LanguageType language, String branchId) {
        return language + "_container_" + branchId;
    }

    private void scheduleContainerTermination(String containerName) {
        ScheduledFuture<?> scheduledTask = scheduler.schedule(() -> stopContainer(containerName),
                TIMEOUT_MINUTES, TimeUnit.MINUTES);

        // Store the scheduled task
        scheduledTasks.put(containerName, scheduledTask);
    }

    public boolean isContainerRunning(String containerName) {
        return runningContainers.containsKey(containerName);
    }

    public void resetTimer(String containerName) {
        // Cancel the existing scheduled task if it exists
        ScheduledFuture<?> scheduledTask = scheduledTasks.get(containerName);
        if (scheduledTask != null) {
            scheduledTask.cancel(false); // Cancel the existing task
        }

        // Schedule a new termination task
        scheduleContainerTermination(containerName);
    }

    public void stopContainer(String containerName) {
        Process process = runningContainers.remove(containerName);
        if (process != null) {
            try {
                stopAndRemoveContainer(containerName);
                scheduledTasks.remove(containerName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stopAndRemoveContainer(String containerName) throws Exception {

        new ProcessBuilder("docker", "stop", containerName).start().waitFor();

        new ProcessBuilder("docker", "rm", containerName).start().waitFor();
    }
}
