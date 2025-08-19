package com.example.memory_guard.user.repository;

import com.example.memory_guard.user.domain.GuardRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuardRequestRepository extends JpaRepository<GuardRequest, Long> {
    Optional<GuardRequest> findGuardRequestById(Long requestId);
}
