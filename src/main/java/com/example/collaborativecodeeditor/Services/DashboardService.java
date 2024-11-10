package com.example.collaborativecodeeditor.Services;

import com.example.collaborativecodeeditor.Entity.Dashboard;
import com.example.collaborativecodeeditor.Repository.DashboardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    @Autowired
    public DashboardService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    public Dashboard getDashboardById(Long id) {
        Optional<Dashboard> dashboardOptional = dashboardRepository.findById(id);
        return dashboardOptional.orElse(null);
    }

    @Transactional
    public Dashboard saveDashboard(Dashboard dashboard) {
        return dashboardRepository.save(dashboard);
    }
}
