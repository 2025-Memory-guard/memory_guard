package com.example.memory_guard.user.repository;

import com.example.memory_guard.user.domain.GuardUserLink;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuardUserLinkRepository extends JpaRepository<GuardUserLink, Long> {
}
