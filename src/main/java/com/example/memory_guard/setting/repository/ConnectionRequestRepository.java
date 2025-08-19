package com.example.memory_guard.setting.repository;

import com.example.memory_guard.setting.domain.ConnectionRequest;
import com.example.memory_guard.setting.domain.RequestStatus;
import com.example.memory_guard.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConnectionRequestRepository extends JpaRepository<ConnectionRequest, Long> {
  boolean existsByRequesterAndReceiverAndStatus(User requester, User receiver, RequestStatus status);

  List<ConnectionRequest> findByReceiverAndStatus(User receiver, RequestStatus status);

  List<ConnectionRequest> findByRequesterAndStatus(User requester, RequestStatus status);
}
