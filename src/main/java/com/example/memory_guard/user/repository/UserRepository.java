package com.example.memory_guard.user.repository;

import com.example.memory_guard.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
  Optional<User> findByUserProfileUserId(String userId);
  Optional<User> findByUserProfileUsername(String username);
  boolean existsByUserProfileUsername(String username);
}
