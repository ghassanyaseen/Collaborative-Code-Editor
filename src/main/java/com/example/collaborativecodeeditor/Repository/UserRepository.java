package com.example.collaborativecodeeditor.Repository;

import com.example.collaborativecodeeditor.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    User findUserByUsername(String username);
}
