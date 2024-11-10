package com.example.collaborativecodeeditor.Services;

import com.example.collaborativecodeeditor.Entity.Dashboard;
import com.example.collaborativecodeeditor.Entity.FileEntity;
import com.example.collaborativecodeeditor.Request.ChangeRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class CodeEditorService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private FileVersionService fileVersionService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private FileService fileService;

    private final ConcurrentHashMap<String, StringBuilder> codeCache = CacheManager.getInstance().getCodeCache();

    private final ReentrantLock saveLock = new ReentrantLock();

    private int oldStart = 0;

    private int oldEnd = 0;

    private int offSet = 0;

    private int oldSize = 0;

    private boolean thereIsWait;

    private boolean delete;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    private void initScheduler() {
        // Schedule the saving of changes to the dashboard table every minute
        scheduler.scheduleAtFixedRate(this::saveDashboardChangesToDB, 1, 1, TimeUnit.MINUTES);
    }

    public void updateCode(ChangeRequest changeRequest) {
        saveLock.lock();
        try {
            StringBuilder currentCode = codeCache.computeIfAbsent(changeRequest.getFileId(), k -> new StringBuilder());


            if(delete && changeRequest.getStart() > oldStart && changeRequest.getEnd() < oldEnd && oldEnd!= 0&& oldStart!=0) {
                messagingTemplate.convertAndSend("/topic/code/" + changeRequest.getFileId(), currentCode.toString());
                delete = false;
                if (saveLock.hasQueuedThreads()) {
                    offSet = currentCode.length() - oldSize;
                    thereIsWait = true;
                } else {
                    offSet = 0;
                    thereIsWait = false;
                }
                return;
            }

            if (thereIsWait && changeRequest.getStart() >= oldStart) {
                if (!(changeRequest.getStart() == oldStart && offSet < 0)) {
                    changeRequest.setStart(changeRequest.getStart() + offSet);
                    changeRequest.setEnd(changeRequest.getEnd() + offSet);
                }
            }


            oldSize = currentCode.length();
            if (changeRequest.getText() != null && !changeRequest.getText().isEmpty()) {
                currentCode.replace(changeRequest.getStart(), changeRequest.getEnd(), changeRequest.getText());
            } else {
                currentCode.delete(changeRequest.getStart(), changeRequest.getEnd());
                delete = true;
            }

            oldStart = changeRequest.getStart();
            oldEnd = changeRequest.getEnd();

            if (thereIsWait) {
                messagingTemplate.convertAndSend("/topic/code/" + changeRequest.getFileId(), currentCode.toString());
            } else {
                messagingTemplate.convertAndSend("/topic/code/" + changeRequest.getFileId(), changeRequest);
            }

            if (saveLock.hasQueuedThreads()) {
                offSet = currentCode.length() - oldSize;
                thereIsWait = true;
            } else {
                offSet = 0;
                thereIsWait = false;
            }

        } finally {
            saveLock.unlock();
        }
    }

    public FileEntity getFileById(Long id) {
        FileEntity file = fileService.getFileById(id);
        StringBuilder cachedContent = codeCache.get(String.valueOf(id));

        if (cachedContent != null) {
            file.setContent(cachedContent.toString());
        } else {
            Dashboard dashboard = dashboardService.getDashboardById(id);
            file.setContent(dashboard != null ? dashboard.getContent() : "");
        }
        return file;
    }

    public Dashboard getDashboardByFileId(Long fileId) {
        Dashboard dashboard = dashboardService.getDashboardById(fileId);
        if (dashboard != null) {
            codeCache.put(String.valueOf(fileId), new StringBuilder(dashboard.getContent()));
            messagingTemplate.convertAndSend("/topic/refresh/" + fileId, "The file has been reverted, please refresh your page.");
        }
        return dashboard;
    }

    public FileEntity revertFile(Long id, Long versionId) throws Exception {
        FileEntity revertedFile = fileVersionService.revertToVersion(id, versionId);
        Dashboard dashboard = dashboardService.getDashboardById(id);

        if (dashboard != null) {
            codeCache.put(String.valueOf(id), new StringBuilder(revertedFile.getContent()));
        }

        messagingTemplate.convertAndSend("/topic/refresh/" + id, "The file has been reverted, please refresh your page.");
        return revertedFile;
    }

    private void saveDashboardChangesToDB() {
        saveLock.lock();
        try {
            codeCache.forEach((fileId, code) -> {
                if (code != null) {
                    saveDashboard(fileId, code.toString());
                }
            });
        } finally {
            saveLock.unlock();
        }
    }

    private void saveDashboard(String fileId, String content) {
        Dashboard dashboard = new Dashboard();
        dashboard.setFileId(Long.valueOf(fileId));
        dashboard.setContent(content);
        dashboard.setUpdatedBy("system");
        dashboard.setTimestamp(LocalDateTime.now());

        dashboardService.saveDashboard(dashboard);
    }

    public void notifyRefresh(Long fileId, String message) {
        messagingTemplate.convertAndSend("/topic/refresh/" + fileId, message);
    }

}
