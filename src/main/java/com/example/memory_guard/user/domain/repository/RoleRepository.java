package com.example.memory_guard.user.domain.repository;

import com.example.memory_guard.user.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(String roleUser);
}
