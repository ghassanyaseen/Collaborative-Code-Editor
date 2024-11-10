package com.example.collaborativecodeeditor.Repository;


import com.example.collaborativecodeeditor.Entity.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DashboardRepository extends JpaRepository<Dashboard, Long> {
}

