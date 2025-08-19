package com.example.memory_guard.user.repository;

import com.example.memory_guard.user.domain.GuardRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuardRequestRepository extends JpaRepository<GuardRequest, Long> {
}
