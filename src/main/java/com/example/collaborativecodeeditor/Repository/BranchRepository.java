package com.example.collaborativecodeeditor.Repository;

import com.example.collaborativecodeeditor.Entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    Branch findByBranchName(String branchName);
}