package com.example.collaborativecodeeditor.Services;


import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {

    private final ConcurrentHashMap<String, StringBuilder> codeCache;

    private CacheManager() {
        codeCache = new ConcurrentHashMap<>();
    }

    private static final class InstanceHolder {
        private static final CacheManager instance = new CacheManager();
    }

    public static CacheManager getInstance() {
        return InstanceHolder.instance;
    }

    public ConcurrentHashMap<String, StringBuilder> getCodeCache() {
        return codeCache;
    }
}
